package com.datagram.datagramweb.Controllers;
import java.util.List;
import com.datagram.datagramweb.Models.Usuario;
import com.datagram.datagramweb.Services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping(value = "/gerente")
public class GerenteController {

  @Autowired
  UsuarioService service;

  @GetMapping(value = "/mediaSeguidores")
  public ResponseEntity<Integer> getNumMediaSeguidores() {
    return ResponseEntity.ok().body(service.numeroMedioSeguidores());
  }

  @GetMapping(value = "/maisConectados")
  public ResponseEntity <List<Usuario>> getMaisConectados() {
    return ResponseEntity.ok().body(service.membrosMaisConectados());
  }
  
}