package com.datagram.datagramweb.controller;

import com.datagram.datagramweb.dto.MemberDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/login")
public class AuthController {
    //Authentication Manager do Spring, responsável por disparar o processo de autenticação.
    @Autowired
    private AuthenticationManager manager;

    @PostMapping
    public ResponseEntity makeLogin(@RequestBody @Valid MemberDTO memberDTO){
        var token = new UsernamePasswordAuthenticationToken(memberDTO.getEmail(), memberDTO.getSenha());
        var authentication = manager.authenticate(token);
        return ResponseEntity.ok().build();
    }
}
