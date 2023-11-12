package com.datagram.datagramweb.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.datagram.datagramweb.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.token.issuer}")
    private String issuer;

    public String generateToken(User user){
        try{
            var algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(issuer)
                    .withSubject(user.getLogin())
                    .withExpiresAt(expirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception){
            throw new RuntimeException("Error while generating Auth Token!", exception);
        }
    }

    public String getSubject(String tokenJWT) {
        try {
            var algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(tokenJWT)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token JWT inválido ou expirado!");
        }
    }

    private Instant expirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
/*
O método .sign() é utilizado para assinar (sign) o token JWT gerado com uma chave de assinatura, usando o algoritmo especificado.

No contexto do código que você forneceu, a função .sign(algorithm) é usada para assinar o token JWT que está sendo criado usando a biblioteca JWT (JSON Web Token). Aqui está uma explicação sobre os métodos utilizados:

JWT.create():
Este método inicia a criação de um novo token JWT.

.withIssuer("API Datagram"):
Define o emissor (issuer) do token como "API Datagram". O emissor identifica quem emitiu o token.

.withSubject(user.getLogin()):
Define o assunto (subject) do token como o login do usuário. O assunto identifica sobre quem o token se trata.

.withExpiresAt(expirationDate()):
Define a data de expiração do token. O método expirationDate() é esperado para gerar uma data de expiração.

.sign(algorithm):
Este é o método que assina o token JWT com um algoritmo específico de assinatura. O parâmetro algorithm é um objeto que representa o algoritmo de assinatura que será utilizado para criar a assinatura do token.

A variável algorithm passada como parâmetro para o método sign() contém o algoritmo de assinatura que será usado para assinar o token. Este algoritmo é fundamental para a segurança do token JWT. O JWT oferece vários algoritmos de assinatura, como por exemplo: HMAC com algoritmos como HS256, HS384 e HS512, e algoritmos assimétricos como RSA com RS256, RS384, RS512, entre outros.

A escolha do algoritmo adequado depende dos requisitos de segurança e das práticas recomendadas para a aplicação. Cada algoritmo tem suas próprias características, como tamanho da chave, robustez, eficiência e segurança. É importante selecionar um algoritmo de assinatura que atenda aos requisitos de segurança da aplicação.

O método .sign(algorithm) é responsável por aplicar a assinatura ao token JWT, garantindo sua integridade e autenticidade. Isso é crucial para garantir que o token não seja alterado ou falsificado durante sua transmissão e utilização.

 */