package com.datagram.datagramweb.config;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
@Configuration
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
 @Bean SecurityFilterChain filterChain(HttpSecurity http, JWTAuthenticationFilter jwt,
   @Value("${app.cors-origin:http://localhost:4200}") String origin) throws Exception {
  return http.csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a.requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
    .requestMatchers(HttpMethod.POST,"/user/login","/user/insert","/municipios").permitAll()
    .requestMatchers(HttpMethod.GET,"/user/*/foto").permitAll()
    .requestMatchers("/gerente/**").hasRole("GERENTE").anyRequest().authenticated())
   .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->res.sendError(401))
    .accessDeniedHandler((req,res,ex)->res.sendError(403)))
   .cors(c->c.configurationSource(req->{var cfg=new org.springframework.web.cors.CorsConfiguration();
    cfg.setAllowedOrigins(java.util.List.of(origin)); cfg.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","OPTIONS"));
    cfg.setAllowedHeaders(java.util.List.of("Authorization","Content-Type"));return cfg;}))
   .addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();
 }
}
