package com.datagram.datagramweb.Controller;

import com.datagram.datagramweb.Domain.Postagem;
import com.datagram.datagramweb.Service.NotificacaoService;
import com.datagram.datagramweb.Service.UsuarioService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/notificacao")
public class NotificacaoController {

    private final NotificacaoService service;
    private final UsuarioService usuarioService;

    public NotificacaoController(NotificacaoService service, UsuarioService usuarioService) {
        this.service = service;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/")
    public ResponseEntity<List<Postagem>> findAllNotifyById() {
        List<Postagem> list = service.findAllbyFollowerId(usuarioService.authenticated().getId());
        Collections.reverse(list);// Reverto a ordem de apresentacao para mostrar da postagem mais recente
        return ResponseEntity.ok().body(list);
    }

}
