package com.datagram.datagramweb.Controller;

import com.datagram.datagramweb.Domain.Postagem;
import com.datagram.datagramweb.Service.NotificacaoService;
import com.datagram.datagramweb.Service.UsuarioService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = "/notificacao")
@org.springframework.transaction.annotation.Transactional
public class NotificacaoController {

  private final NotificacaoService service;
  private final UsuarioService usuarioService;

  public NotificacaoController(NotificacaoService service, UsuarioService usuarioService) {
    this.service = service;
    this.usuarioService = usuarioService;
  }

  @GetMapping("/")
  public ResponseEntity<List<com.datagram.datagramweb.api.ApiDtos.PostView>> findAllNotifyById() {
    List<Postagem> list = service.findAllbyFollowerId(usuarioService.authenticated().getId());

    return ResponseEntity.ok()
        .body(
            list.stream()
                .map(
                    p ->
                        com.datagram.datagramweb.api.ApiDtos.PostView.of(
                            p, usuarioService.authenticated().getId()))
                .toList());
  }
}
