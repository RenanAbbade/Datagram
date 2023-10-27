package com.datagram.datagramweb.controller;

import com.datagram.datagramweb.domain.Postagem;
import com.datagram.datagramweb.service.NotificacaoService;
import com.datagram.datagramweb.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/notification")
public class NotificationController {

    @Autowired
    NotificacaoService service;

    @GetMapping(value = "/")
    public ResponseEntity<List<Postagem>> findAllNotifyById() {
        List<Postagem> list = service.findAllbyFollowerId(UsuarioService.memberLogado.getId());
        Collections.reverse(list);// Reverto a ordem de apresentacao para mostrar da postagem mais recente
        return ResponseEntity.ok().body(list);
    }

}
