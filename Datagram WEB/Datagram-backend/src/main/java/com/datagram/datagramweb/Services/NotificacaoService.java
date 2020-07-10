package com.datagram.datagramweb.Services;

import java.util.ArrayList;
import java.util.List;

import com.datagram.datagramweb.Models.Notificacao;
import com.datagram.datagramweb.Models.Postagem;
import com.datagram.datagramweb.Models.Usuario;
import com.datagram.datagramweb.Repositories.NotificacaoRepository;
import com.datagram.datagramweb.Repositories.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoService {

  @Autowired
  private NotificacaoRepository repo;

  @Autowired
  private UsuarioRepository uRepository;

  @Autowired
  private UsuarioService uService;

  @Autowired
  private PostagemService pService;

  public void createNotify(Postagem obj) {
    // Compartilhando notificacao da postagem realizada com os seguidores do usuário
    // logado.
    for (Integer i : UsuarioService.usuarioLogado.getIdsSeguidores()) {
      Usuario seguidor = uService.find(i);
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

    for (Notificacao notify : UsuarioService.usuarioLogado.getNotificacoes()) {

      Postagem post = pService.find(notify.getPostagem_id());

      notificacoes.add(post);
    }
    return notificacoes;
  }

}