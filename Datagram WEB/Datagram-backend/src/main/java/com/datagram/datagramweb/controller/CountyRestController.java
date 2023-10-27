package com.datagram.datagramweb.controller;

import java.util.List;

import com.datagram.datagramweb.service.MunicipioRestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = "/municipios")
public class CountyRestController {

  @Autowired
  MunicipioRestService service;

  @ResponseBody
  @PostMapping
  public ResponseEntity<List<String>> listCounty(@RequestBody String Uf) {
    return ResponseEntity.ok(service.listarMunicipios(Uf));

  }

}