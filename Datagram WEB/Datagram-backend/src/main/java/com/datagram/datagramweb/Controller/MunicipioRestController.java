package com.datagram.datagramweb.Controller;

import com.datagram.datagramweb.Service.MunicipioRestService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = "/municipios")
public class MunicipioRestController {

  private final MunicipioRestService service;

  public MunicipioRestController(MunicipioRestService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<List<String>> municipios(@RequestBody String Uf) {
    return ResponseEntity.ok(service.listarMunicipios(Uf));
  }
}
