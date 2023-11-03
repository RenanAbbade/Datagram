package com.datagram.datagramweb.controller;

import com.datagram.datagramweb.domain.Member;
import com.datagram.datagramweb.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/manager")
public class ManagerController {

  @Autowired
  private UsuarioService service;

  @GetMapping(value = "/follower-average")
  public ResponseEntity<Integer> followersAverageNumber() {
    return ResponseEntity.ok().body(service.followersAverageNumber());
  }

  @GetMapping(value = "/more-connected")
  public ResponseEntity <List<Member>> moreConnectedMembers() {
    return ResponseEntity.ok().body(service.moreConnectedMembers());
  }
  
}