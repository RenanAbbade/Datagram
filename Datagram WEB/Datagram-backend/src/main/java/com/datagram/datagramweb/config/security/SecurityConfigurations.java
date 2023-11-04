package com.datagram.datagramweb.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
/*
 * A próxima alteração é configurar o Spring Security para ele não usar o processo de segurança tradicional, o stateful. Como estamos trabalhando com uma API Rest, o processo de autenticação precisa ser stateless.
 *
 * */
@Configuration
@EnableWebSecurity
public class SecurityConfigurations  {

    /*
     * Estamos desabilitando o tratamento contra-ataque CSRF e, na sequência, desabilitando o processo de autenticação padrão do Spring. Isso porque estamos usando uma API Rest, e queremos que seja stateless.
     *  O Spring não lê o método de forma automática, precisamos incluir uma anotação. No caso, o @Bean, que serve exibir o retorno desse método, que estamos devolvendo um objeto SecurityFilterChain.
     *
     * */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().build();
    }

    @Bean
    //A anotação @Bean serve para exportar uma classe para o Spring, fazendo com que ele consiga carregá-la e realizar a sua injeção de dependência em outras classes.
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    //. Serve para desabilitarmos proteção contra-ataques do tipo CSRF (Cross-Site Request Forgery).Estamos desabilitando esse tipo de ataque porque vamos trabalhar com autenticação via tokens. Nesse cenário, o próprio token é uma proteção contra esses tipos de ataques e ficaria repetitivo.
//sessionManagement(), para mostrar o gerenciamento da sessão. Na sequência, mais um ponto e o método sessionCreationPolicy(), qual a política de criação da sessão.

    @Bean //Será público e devolve um objeto do tipo PasswordEncoder, sendo a classe que representa o algoritmo de hashing da senha.
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    //Como método de criptografia de senhas, utilizamos o método de hashing Bcrypt.

}



