package com.datagram.datagramweb.controller;

import com.datagram.datagramweb.config.security.TokenService;
import com.datagram.datagramweb.domain.User;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@Slf4j
public class AuthController {
    //Authentication Manager do Spring, responsável por disparar o processo de autenticação.
    @Autowired
    private AuthenticationManager manager;
    @Autowired
    private TokenService tokenService;


    @PostMapping
    public ResponseEntity makeLogin(@RequestBody @Valid User user) {
        var token = new UsernamePasswordAuthenticationToken(user.getLogin(), user.getPassword());
        try {
            var authentication = manager.authenticate(token);
            return ResponseEntity.ok(tokenService.generateToken((User) authentication.getPrincipal()));
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(e.toString());
            throw e;
        }
    }
}
