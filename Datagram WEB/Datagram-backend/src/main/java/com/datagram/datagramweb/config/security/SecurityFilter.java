package com.datagram.datagramweb.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
/*
* vamos criar um filtro no projeto, para interceptar requisições. O que queremos é fazer a validação do token antes que ele caia no controller
* */
@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var tokenJWT = tokenRecovery(request);
        logger.info("Token Generated: " + tokenJWT);
        var subject = tokenService.getSubject(tokenJWT);
        logger.info("Token Subject: " + subject);
        filterChain.doFilter(request, response);
    }

    private String tokenRecovery(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null) {
            throw new RuntimeException("Token JWT não enviado no cabeçalho Authorization!");
        }

        return authorizationHeader.replace("Bearer ", "");
    }
}