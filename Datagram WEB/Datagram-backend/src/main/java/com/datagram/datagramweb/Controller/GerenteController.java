package com.datagram.datagramweb.Controller;

import com.datagram.datagramweb.Service.UsuarioService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = "/gerente")
@org.springframework.transaction.annotation.Transactional
public class GerenteController {

  private final UsuarioService service;

  public GerenteController(UsuarioService service) {
    this.service = service;
  }

  @GetMapping("/mediaSeguidores")
  public ResponseEntity<Integer> getNumMediaSeguidores() {
    return ResponseEntity.ok().body(service.numeroMedioSeguidores());
  }

  @GetMapping("/maisConectados")
  public ResponseEntity<List<com.datagram.datagramweb.api.ApiDtos.UserView>> getMaisConectados() {
    return ResponseEntity.ok()
        .body(
            service.membrosMaisConectados().stream()
                .map(u -> com.datagram.datagramweb.api.ApiDtos.UserView.of(u, false))
                .toList());
  }
}
