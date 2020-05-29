package com.datagram.datagramweb.Controllers;

import java.util.List;

import com.datagram.datagramweb.Services.MunicipioRestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = "/municipios")
public class MunicipioRestController {

@Autowired
MunicipioRestService service;


@ResponseBody
@PostMapping
public ResponseEntity <List<String>> municipios(@RequestBody String Uf){
  return ResponseEntity.ok(service.listarMunicipios(Uf));
    
  }


  
}