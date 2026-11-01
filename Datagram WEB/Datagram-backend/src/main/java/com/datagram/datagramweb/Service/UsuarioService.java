package com.datagram.datagramweb.Service;

import java.util.*;

import com.datagram.datagramweb.Domain.Usuario;
import com.datagram.datagramweb.Repository.UsuarioRepository;
import com.datagram.datagramweb.config.JWTUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;

    @Autowired
    private JWTUtil jwtUtil;

    public UsuarioService(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // CREATE
    public Usuario insert(Usuario obj) {
        obj.setId(null);
        obj.setEmail(obj.getEmail().trim().toLowerCase(java.util.Locale.ROOT));
        obj.setSenha(passwordEncoder.encode(obj.getSenha()));
        return repo.save(obj);
    }

    // READ
    public Usuario find(Integer id) {
        Optional<Usuario> obj = repo.findById(id);
        return obj.orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"Usuário não encontrado"));
    }

    // READ ALL
    public Iterable<Usuario> findAll() {
        return repo.findAll();
    }

    @org.springframework.transaction.annotation.Transactional
    public Usuario updateProfile(com.datagram.datagramweb.api.ApiDtos.Profile data) {
        Usuario u=authenticated();
        u.setNome(data.nome().trim()); u.setEscolaridade(data.escolaridade());
        u.setEstado(data.estado()); u.setMunicipio(data.municipio());
        u.setInstituicao(data.instituicao()); u.setLinkCv(safeUrl(data.linkCv()));
        u.setInteresses(data.interesses()==null?new HashSet<>():new HashSet<>(data.interesses()));
        return repo.save(u);
    }

    public static String safeUrl(String value) {
        if(value==null || value.isBlank()) return null;
        try {
            var uri=java.net.URI.create(value);
            if (!Set.of("https","http").contains(uri.getScheme()) || uri.getHost()==null) throw new IllegalArgumentException();
            return uri.toString();
        } catch(IllegalArgumentException e) { throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"URL deve usar http ou https"); }
    }

    @org.springframework.transaction.annotation.Transactional
    public Usuario follow(Integer id, boolean follow) {
        Usuario me=authenticated(), target=find(id);
        if(me.getId().equals(id)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"Não é possível seguir a si mesmo");
        if(follow) { me.getIdsSeguindo().add(id); target.getIdsSeguidores().add(me.getId()); }
        else { me.getIdsSeguindo().remove(id); target.getIdsSeguidores().remove(me.getId()); }
        me.setSeguindo(me.getIdsSeguindo().size()); target.setSeguidores(target.getIdsSeguidores().size());
        repo.save(me); return repo.save(target);
    }

    @org.springframework.transaction.annotation.Transactional
    public Map<String,String> login(String email, String senha) {
        Usuario u=repo.findByEmail(email.trim().toLowerCase(java.util.Locale.ROOT));
        if (u==null || senha==null || u.getSenha()==null || !passwordEncoder.matches(senha,u.getSenha()))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED,"Credenciais inválidas");
        return Map.of("token",jwtUtil.generateToken(u));
    }

    public boolean validaExistenciaEmail(String email) {
        Usuario obj = repo.findByEmail(email);
        if (obj == null)// Ainda não existe esse email no BD
            return false;
        return true;
    }

    // Método auxiliar para obter o usuário autenticado via SecurityContextHolder
    public Usuario authenticated() {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth==null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName()))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED,"Autenticação necessária");
        Usuario u=repo.findByEmail(auth.getName());
        if(u==null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED,"Sessão inválida");
        return u;
    }

    public Usuario findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public List<Usuario> findByNome(String nome) {
        List<Usuario> usuariosPesquisa = repo.findByNome(nome);

        Usuario authenticatedUser = authenticated();
        usuariosPesquisa.removeIf(x -> x.getId().equals(authenticatedUser.getId()));// irá remover o usuario logado do retorno da
                                                                        // pesquisa

        return usuariosPesquisa;
    }

    public List<Usuario> findByInstituicao(String instituicao) {
        List<Usuario> usuariosPesquisa = repo.findByInstituicao(instituicao);
        Usuario authenticatedUser = authenticated();
        usuariosPesquisa.removeIf(x -> x.getId().equals(authenticatedUser.getId()));

        return usuariosPesquisa;
    }

    public List<Usuario> findUsersByInteresses() {
        Usuario authenticatedUser = authenticated();
        Set<String> interessesLogado = authenticatedUser.getInteresses();
        Set<Integer> seguindoLogado = authenticatedUser.getIdsSeguindo();
        Set<Integer> seguidoresLogado = authenticatedUser.getIdsSeguidores();

        List<Usuario> usuarios = repo.findAll();
        List<Usuario> usuariosMutuos = new ArrayList<Usuario>();

        usuarios.removeIf(x -> x.getId().equals(authenticatedUser.getId()));

        for (Usuario user : usuarios) {
            if (!checkFollower(user.getId())) {
                if (CollectionUtils.containsAny(interessesLogado, user.getInteresses())
                        && CollectionUtils.containsAny(seguindoLogado, user.getIdsSeguindo())
                        && CollectionUtils.containsAny(seguidoresLogado, user.getIdsSeguidores())) {
                    usuariosMutuos.add(user);
                    if (usuariosMutuos.size() == 3)
                        break;
                }
            }
        }
        return usuariosMutuos;
    }

    public Boolean checkFollower(Integer id) {
        Usuario authenticatedUser = authenticated();
        Set<Integer> seguindoLogado = authenticatedUser.getIdsSeguindo();
        return seguindoLogado.contains(id);
    }

    public Integer numeroMedioSeguidores() {
        List<Usuario> usuarios = repo.findAll();
        Integer totalSeguidores = 0;
        for (Usuario usuario : usuarios) {
            totalSeguidores += usuario.getSeguidores() != null ? usuario.getSeguidores() : 0;
        }
        return (usuarios.size() != 0 && totalSeguidores != 0) ? (totalSeguidores / usuarios.size()) : 0;
    }

    public List<Usuario> membrosMaisConectados() {
        Map<Usuario, Integer> usuariosSeguidores = new HashMap<>();

        List<Usuario> usuarios = (!repo.findAll().isEmpty()) ? sortUserByNumberOfFollowers(repo.findAll()) : new ArrayList<>();
        List<Usuario> usuariosMaisConectados = new ArrayList<>();

        if (usuarios.size() > 10) {// iterator performa melhor que o for até o list.size()
            Iterator<Usuario> i = usuarios.iterator();

            while (i.hasNext() && usuariosMaisConectados.size() < 10)
                usuariosMaisConectados.add(i.next());

        } else
            return usuariosMaisConectados = usuarios;

        return usuariosMaisConectados;
    }

    public List<Usuario> sortUserByNumberOfFollowers(List<Usuario> myList) {
        myList.sort(new Comparator<Usuario>() {
            @Override
            public int compare(Usuario usuario1, Usuario usuario2) {
                if (usuario1.getSeguidores() == null || usuario2.getSeguidores() == null)
                      return 0;
                return usuario1.getSeguidores().compareTo(usuario2.getSeguidores());
            }
        });
        Collections.reverse(myList);
        return myList;
    }

}
