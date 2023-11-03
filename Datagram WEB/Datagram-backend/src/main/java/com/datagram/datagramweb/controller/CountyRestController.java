package com.datagram.datagramweb.controller;

import com.datagram.datagramweb.service.MunicipioRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/municipios")
public class CountyRestController {

  @Autowired
  private MunicipioRestService service;

  @ResponseBody
  @PostMapping
  public ResponseEntity<List<String>> listCounty(@RequestBody String Uf) {
    return ResponseEntity.ok(service.listarMunicipios(Uf));

  }

}