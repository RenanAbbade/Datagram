package com.datagram.datagramweb.Service;

import com.datagram.datagramweb.Domain.Usuario;
import com.datagram.datagramweb.Repository.UsuarioRepository;
import com.datagram.datagramweb.api.ApiDtos.Profile;
import com.datagram.datagramweb.config.JWTUtil;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class UsuarioService {
  private final UsuarioRepository repo;
  private final JWTUtil jwt;
  private final PasswordEncoder passwords;

  public UsuarioService(UsuarioRepository repo, JWTUtil jwt, PasswordEncoder passwords) {
    this.repo = repo;
    this.jwt = jwt;
    this.passwords = passwords;
  }

  @Transactional
  public Usuario insert(Usuario u) {
    u.setId(null);
    u.setEmail(u.getEmail().trim().toLowerCase(Locale.ROOT));
    u.setSenha(passwords.encode(u.getSenha()));
    return repo.save(u);
  }

  public Usuario find(Integer id) {
    return repo.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
  }

  public Usuario findByEmail(String email) {
    return repo.findByEmail(email);
  }

  public boolean validaExistenciaEmail(String email) {
    return repo.findByEmail(email) != null;
  }

  public Usuario authenticated() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName()))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticação necessária");
    Usuario user = repo.findByEmail(auth.getName());
    if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão inválida");
    return user;
  }

  public Map<String, String> login(String email, String password) {
    Usuario user = repo.findByEmail(email.trim().toLowerCase(Locale.ROOT));
    if (user == null
        || password == null
        || user.getSenha() == null
        || !passwords.matches(password, user.getSenha()))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
    return Map.of("token", jwt.generateToken(user));
  }

  @Transactional
  public Usuario updateProfile(Profile data) {
    Usuario u = authenticated();
    u.setNome(data.nome().trim());
    u.setEscolaridade(data.escolaridade());
    u.setEstado(data.estado());
    u.setMunicipio(data.municipio());
    u.setInstituicao(data.instituicao());
    u.setLinkCv(safeUrl(data.linkCv()));
    u.setInteresses(
        data.interesses() == null
            ? new HashSet<>()
            : data.interesses().stream()
                .map(String::trim)
                .filter(i -> !i.isBlank())
                .collect(Collectors.toSet()));
    return repo.save(u);
  }

  @Transactional
  public Usuario follow(Integer id, boolean follow) {
    Usuario me = authenticated(), target = find(id);
    if (me.getId().equals(id))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível seguir a si mesmo");
    if (follow) {
      me.getIdsSeguindo().add(id);
      target.getIdsSeguidores().add(me.getId());
    } else {
      me.getIdsSeguindo().remove(id);
      target.getIdsSeguidores().remove(me.getId());
    }
    me.setSeguindo(me.getIdsSeguindo().size());
    target.setSeguidores(target.getIdsSeguidores().size());
    repo.save(me);
    return repo.save(target);
  }

  public boolean checkFollower(Integer id) {
    return authenticated().getIdsSeguindo().contains(id);
  }

  public List<Usuario> findByNome(String nome) {
    Integer id = authenticated().getId();
    return repo.findByNomeStartingWithIgnoreCase(nome.trim(), PageRequest.of(0, 50)).stream()
        .filter(u -> !u.getId().equals(id))
        .toList();
  }

  public List<Usuario> findByInstituicao(String institution) {
    Integer id = authenticated().getId();
    return repo
        .findByInstituicaoStartingWithIgnoreCase(institution.trim(), PageRequest.of(0, 50))
        .stream()
        .filter(u -> !u.getId().equals(id))
        .toList();
  }

  public List<Usuario> findUsersByInteresses() {
    Usuario me = authenticated();
    Set<String> tags =
        me.getInteresses().stream()
            .map(s -> s.toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());
    return tags.isEmpty() ? List.of() : repo.suggestions(me.getId(), tags, PageRequest.of(0, 3));
  }

  public Integer numeroMedioSeguidores() {
    return repo.averageFollowers().intValue();
  }

  public List<Usuario> membrosMaisConectados() {
    return repo.findTop10ByOrderBySeguidoresDescIdDesc();
  }

  public static String safeUrl(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      var uri = java.net.URI.create(value);
      if (!Set.of("https", "http").contains(uri.getScheme()) || uri.getHost() == null)
        throw new IllegalArgumentException();
      return uri.toString();
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL deve usar http ou https");
    }
  }
}
