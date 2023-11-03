package com.datagram.datagramweb.service;

import java.util.ArrayList;
import java.util.List;

import com.datagram.datagramweb.domain.Notificacao;
import com.datagram.datagramweb.domain.Postagem;
import com.datagram.datagramweb.domain.Member;
import com.datagram.datagramweb.repository.MemberRepository;
import com.datagram.datagramweb.repository.NotificacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoService {

  @Autowired
  private NotificacaoRepository repo;

  @Autowired
  private MemberRepository uRepository;

  @Autowired
  private UsuarioService uService;

  @Autowired
  private PostagemService pService;

  public void createNotify(Postagem obj) {
    // Compartilhando notificacao da postagem realizada com os seguidores do usuário
    // logado.
    for (Integer i : UsuarioService.memberLogado.getIdsSeguidores()) {
      Member seguidor = uService.find(i);
      var notificacao = new Notificacao(seguidor, obj.getId());
      seguidor.getNotificacoes().add(notificacao);
      repo.save(notificacao);
      uRepository.save(seguidor);
    }
  }

  public Notificacao find(Integer id) {
    return repo.findById(id).get();
  }

  public List<Postagem> findAllbyFollowerId(Integer id) {

    List<Postagem> notificacoes = new ArrayList<>();

    for (Notificacao notify : UsuarioService.memberLogado.getNotificacoes()) {

      Postagem post = pService.find(notify.getPostagem_id());

      notificacoes.add(post);
    }
    return notificacoes;
  }

}