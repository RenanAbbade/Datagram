package com.datagram.datagramweb.config;

import com.datagram.datagramweb.Domain.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

  @Autowired private JWTUtil jwtUtil;

  @Autowired private com.datagram.datagramweb.Repository.UsuarioRepository usuarioRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    final String requestTokenHeader = request.getHeader("Authorization");

    String username = null;
    String jwtToken = null;

    // JWT Token está no form "Bearer token". Remove Bearer word e pega apenas o Token
    if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
      jwtToken = requestTokenHeader.substring(7);
      try {
        username = jwtUtil.getUsernameFromToken(jwtToken);
      } catch (Exception e) {
        logger.warn("JWT Token inválido ou expirado");
      }
    }

    // Uma vez que obtemos o token, validamos.
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

      Usuario usuario = usuarioRepository.findByEmail(username);

      if (usuario != null && jwtUtil.validateToken(jwtToken, username)) {

        // Se token válido, configura Spring Security para definir autenticação manualmente
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
            new UsernamePasswordAuthenticationToken(
                username,
                null,
                java.util.List.of(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_"
                            + usuario
                                .getTipoUsuario()
                                .toUpperCase(
                                    java.util.Locale.ROOT)))); // Sem authorities por simplicidade

        usernamePasswordAuthenticationToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request));

        // Após definir o Authentication no contexto, especificamos que o usuário atual está
        // autenticado.
        // Então passa para o próximo filtro
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
      }
    }
    chain.doFilter(request, response);
  }
}
