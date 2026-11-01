package com.datagram.datagramweb.Service;

import com.datagram.datagramweb.Domain.*;
import com.datagram.datagramweb.Repository.*;
import com.datagram.datagramweb.api.ApiDtos.PostInput;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class PostagemService {
  private final PostagemRepository repo;
  private final UsuarioRepository users;
  private final UsuarioService session;
  private final NotificacaoRepository notifications;

  public PostagemService(
      PostagemRepository p, UsuarioRepository u, UsuarioService s, NotificacaoRepository n) {
    repo = p;
    users = u;
    session = s;
    notifications = n;
  }

  public Postagem find(Integer id) {
    return repo.findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Postagem não encontrada"));
  }

  public List<Postagem> findAllbyAutorId(Integer id) {
    session.find(id);
    return repo.findTop50ByAutorIdOrderByIdDesc(id);
  }

  public Slice<Postagem> page(Integer author, int page, int size) {
    if (page < 0 || size < 1 || size > 50)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paginação inválida");
    var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    if (author != null) {
      session.find(author);
      return repo.findByAutorId(author, pageable);
    }
    var me = session.authenticated();
    Set<Integer> ids = new HashSet<>(me.getIdsSeguindo());
    ids.add(me.getId());
    return repo.findByAutorIdIn(ids, pageable);
  }

  private void apply(Postagem p, PostInput d) {
    p.setTitulo(d.titulo().trim());
    p.setSubtitulo(d.subtitulo());
    p.setConteudo(d.conteudo().trim());
    p.setURL(UsuarioService.safeUrl(d.url()));
    if (!Set.of("simples", "cientifica").contains(d.tipoPostagem()))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de postagem inválido");
    p.setTipoPostagem(d.tipoPostagem());
    p.setPalavrasChave(
        d.palavrasChave() == null ? new HashSet<>() : new HashSet<>(d.palavrasChave()));
  }

  public Postagem own(Integer id) {
    var p = find(id);
    if (!p.getAutor().getId().equals(session.authenticated().getId()))
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Somente o autor pode alterar esta postagem");
    return p;
  }

  @Transactional
  public Postagem insert(PostInput d, String attachment) {
    Usuario me = session.authenticated();
    Postagem p = new Postagem();
    apply(p, d);
    p.setAutor(me);
    p.setDate(java.time.Instant.now().toString());
    p.setArquivoPublicacao(attachment);
    p = repo.save(p);
    me.setPosts(1);
    users.save(me);
    for (Integer follower : me.getIdsSeguidores())
      notifications.save(new Notificacao(session.find(follower), p.getId()));
    return p;
  }

  @Transactional
  public Postagem update(Integer id, PostInput d) {
    Postagem p = own(id);
    apply(p, d);
    return repo.save(p);
  }

  @Transactional
  public Postagem like(Integer id, boolean like) {
    Postagem p = find(id);
    Integer me = session.authenticated().getId();
    Set<Integer> likes = new HashSet<>(p.getIdsCurtida());
    if (like) likes.add(me);
    else likes.remove(me);
    p.setAllIdsCurtida(likes);
    return repo.save(p);
  }

  @Transactional
  public void delete(Integer id) {
    Postagem p = own(id);
    var author = p.getAutor();
    notifications.deleteForPost(id);
    repo.delete(p);
    author.setPosts(-1);
    users.save(author);
  }

  public List<Postagem> findPostsMaisCurtidos() {
    return repo.findTop10ByAutorIdOrderByCurtidaDescIdDesc(session.authenticated().getId());
  }
}
