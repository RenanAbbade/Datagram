# Migração para Spring Security + JWT: Resolvendo o Estado Estático Global

## Introdução

Este documento explica a migração do sistema de autenticação da API REST Datagram de um modelo problemático (estado estático global com `public static Usuario usuarioLogado`) para uma arquitetura moderna usando Spring Security + JWT (JSON Web Tokens). O objetivo é permitir acesso simultâneo de múltiplos usuários, eliminando o gargalo que limitava a aplicação a um usuário logado por vez.

### Problema Original

- **Estado Compartilhado Global**: O campo `public static Usuario usuarioLogado` no `UsuarioService` era compartilhado por toda a aplicação JVM. Isso significava que apenas um usuário poderia estar "logado" em qualquer momento, pois o campo era sobrescrito a cada login.
- **Impacto**: Em um ambiente de produção com múltiplos usuários acessando simultaneamente (ex.: via Angular front-end), isso causava conflitos, dados incorretos e impossibilidade de uso real.
- **Anti-Padrão**: Estados estáticos em aplicações web violam princípios de escalabilidade e isolamento de usuários.

### Solução: Spring Security + JWT

- **Spring Security**: Framework padrão do Spring para autenticação e autorização, configurado para ser stateless (sem sessões no servidor).
- **JWT**: Tokens seguros que contêm informações do usuário (ex.: ID, email), assinados digitalmente. Cada requisição do front-end inclui o token no header `Authorization: Bearer <token>`.
- **Benefícios**:
  - **Multiusuário**: Cada requisição carrega seu próprio contexto de usuário.
  - **Stateless**: Não há estado no servidor; ideal para APIs REST e escalabilidade.
  - **Segurança**: Tokens podem expirar, serem invalidados e são resistentes a tampering.
  - **Integração com Front-End**: O Angular pode armazenar o token no localStorage e enviá-lo automaticamente em cada requisição.

## Arquitetura Após Migração

1. **Login**: Usuário envia email/senha. Se válido, gera-se um JWT com dados do usuário.
2. **Requisições Autenticadas**: Front-end inclui `Authorization: Bearer <token>` em headers.
3. **Filtro JWT**: Intercepta requisições, valida token e define o usuário no `SecurityContextHolder`.
4. **Serviços**: Usam `SecurityContextHolder` para obter o usuário logado por requisição.

### Componentes Implementados

#### 1. SecurityConfig (Configuração de Segurança)
- **Localização**: `src/main/java/com/datagram/datagramweb/config/SecurityConfig.java`
- **Função**: Configura Spring Security para desabilitar CSRF (não necessário em APIs stateless), definir política de sessão como `STATELESS` e permitir CORS para o front-end Angular (porta 4200).
- **Detalhes**:
  - CSRF desabilitado porque JWT protege contra ataques CSRF via tokens.
  - Sessões stateless: Não cria `HttpSession`, economizando recursos.
  - Permite acesso público a endpoints de login e registro, mas protege outros com autenticação.

#### 2. JWTUtil (Utilitário para Tokens)
- **Localização**: `src/main/java/com/datagram/datagramweb/config/JWTUtil.java`
- **Função**: Classe utilitária para gerar e validar JWTs.
- **Métodos Principais**:
  - `generateToken(Usuario usuario)`: Cria um token com claims (ID, email, expiração).
  - `validateToken(String token)`: Verifica assinatura e expiração.
  - `getUsernameFromToken(String token)`: Extrai o email do token.
- **Segurança**: Usa chave secreta (hardcoded aqui para simplicidade; em produção, use variáveis de ambiente). Tokens expiram em 24 horas.

#### 3. JWTAuthenticationFilter (Filtro de Autenticação)
- **Localização**: `src/main/java/com/datagram/datagramweb/config/JWTAuthenticationFilter.java`
- **Função**: Filtro que intercepta requisições HTTP, extrai o token do header `Authorization`, valida-o e autentica o usuário no `SecurityContextHolder`.
- **Fluxo**:
  - Se header presente e válido, carrega o usuário do banco via `UsuarioService.findByEmail`.
  - Define `UsernamePasswordAuthenticationToken` no contexto de segurança.
- **Integração**: Registrado no `SecurityConfig` como filtro antes do `UsernamePasswordAuthenticationFilter`.

#### 4. Refatoração do UsuarioService
- **Localização**: `src/main/java/com/datagram/datagramweb/Service/UsuarioService.java`
- **Mudanças**:
  - Removido: Campo `public static Usuario usuarioLogado`.
  - Removido: Método `getUsuarioLogado()`.
  - Adicionado: Método `authenticated()` que retorna o usuário logado via `SecurityContextHolder.getContext().getAuthentication().getPrincipal()`.
  - Refatorado: `login()` agora retorna um `Map<String, String>` com o token JWT em vez de definir o estado estático.
  - Impacto: Métodos que dependiam de `usuarioLogado` (ex.: `findByNome`, `findUsersByInteresses`) agora usam `authenticated()`.

#### 5. Exemplo de Refatoração em PostagemService
- **Localização**: `src/main/java/com/datagram/datagramweb/Service/PostagemService.java`
- **Mudanças**:
  - Removido: Uso direto de `UsuarioService.usuarioLogado`.
  - Adicionado: Injeção de `UsuarioService` (se necessário) e uso de `usuarioService.authenticated()` para obter o autor.
  - Exemplo: No método `insert()`, `obj.setAutor(usuarioService.authenticated());`.

## Como Funciona o Fluxo Completo

1. **Registro/Login**:
   - Front-end (Angular) envia POST para `/user/login` com email/senha.
   - `UsuarioController.login()` valida credenciais via `UsuarioService.login()`.
   - Se válido, gera JWT via `JWTUtil.generateToken()` e retorna `{"token": "<jwt>"}`.

2. **Requisições Autenticadas**:
   - Front-end armazena token e inclui em headers: `Authorization: Bearer <token>`.
   - `JWTAuthenticationFilter` valida token, carrega usuário e define no contexto.

3. **Uso em Serviços**:
   - Qualquer serviço pode chamar `UsuarioService.authenticated()` para obter o usuário da requisição atual.
   - Ex.: Criar postagem define autor automaticamente sem estado global.

## Benefícios para o Front-End (Angular)

- **Armazenamento de Token**: Use `localStorage` ou `sessionStorage` para persistir o token.
- **Interceptors HTTP**: Configure um interceptor para adicionar automaticamente o header `Authorization` em todas as requisições autenticadas.
- **Logout**: Remova o token do storage; o servidor não mantém estado.
- **Tratamento de Erros**: Se token expirar (401), redirecione para login.

## Considerações de Segurança e Produção

- **Chave Secreta**: Mova `JWTUtil.JWT_SECRET` para variáveis de ambiente (ex.: `application.properties` ou Docker secrets).
- **HTTPS**: Sempre use HTTPS para proteger tokens em trânsito.
- **Refresh Tokens**: Para sessões longas, implemente refresh tokens (não incluído aqui para simplicidade).
- **Validação Adicional**: Adicione roles/autorizações se necessário (ex.: `@PreAuthorize`).
- **Testes**: Atualize testes unitários para mockar `SecurityContextHolder`.

## Conclusão

Esta migração transforma a aplicação em uma API moderna, escalável e segura, suportando múltiplos usuários simultâneos. O estado estático foi eliminado, e o Spring Security garante isolamento por requisição. Para dúvidas ou ajustes, consulte a documentação do Spring Security ou os comentários no código.
