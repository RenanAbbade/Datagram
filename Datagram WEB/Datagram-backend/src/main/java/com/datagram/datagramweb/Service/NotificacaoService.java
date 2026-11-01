package com.datagram.datagramweb.Service;

import java.util.ArrayList;
import java.util.List;

import com.datagram.datagramweb.Domain.Notificacao;
import com.datagram.datagramweb.Domain.Postagem;
import com.datagram.datagramweb.Domain.Usuario;
import com.datagram.datagramweb.Repository.NotificacaoRepository;
import com.datagram.datagramweb.Repository.UsuarioRepository;

import org.springframework.stereotype.Service;

@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class NotificacaoService {

    private final NotificacaoRepository repo;
    private final UsuarioRepository uRepository;
    private final UsuarioService uService;
    private final PostagemService pService;

    public NotificacaoService(NotificacaoRepository repo, UsuarioRepository uRepository, UsuarioService uService, PostagemService pService) {
        this.repo = repo;
        this.uRepository = uRepository;
        this.uService = uService;
        this.pService = pService;
    }

    public void createNotify(Postagem obj) {
        // Compartilhando notificacao da postagem realizada com os seguidores do usuário
        // logado.
        for (Integer i : uService.authenticated().getIdsSeguidores()) {
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

        for (Notificacao notify : uService.authenticated().getNotificacoes()) {

            Postagem post = pService.find(notify.getPostagem_id());

            notificacoes.add(post);
        }
        return notificacoes;
    }

}