# Datagram

Rede social para pesquisadores: perfil, foto, publicações e PDFs, curtidas,
seguidores, busca por nome/instituição, sugestões e notificações.

## Aplicação web

- Backend: Java 21, Spring Boot 3.2.9, Spring Security/JWT, JPA e Bean Validation.
- Frontend: Angular 22.2.4 standalone, Signals, RxJS e formulários reativos tipados.
- Layout compartilhado, páginas carregadas sob demanda e API com DTOs explícitos.
- Refatoração realizada na branch `refatoracao-codex`; sem merge na `master`.

### Executar localmente

Use JDK 21 e Node 24 (versão de referência em `.nvmrc` no frontend).

```sh
cd "Datagram WEB/Datagram-backend"
./mvnw spring-boot:run
```

Em outro terminal:

```sh
cd "Datagram WEB/Datagram-frontend"
npm ci
npm start
```

Abra http://localhost:4200. O proxy do Angular encaminha `/api` ao backend em
http://localhost:8080. O banco H2 padrão é em memória: reiniciar apaga os dados de
desenvolvimento. Cadastre uma conta nova; não há senha administrativa padrão.

### Validar

```sh
./scripts/verify.sh
```

Para testes de navegador, instale antes `npx playwright install --with-deps chromium`
no frontend e execute `E2E=true ./scripts/verify.sh`. O Playwright inicia frontend e
backend, sem exigir contas reais. Não execute contra uma instância com dados reais.

### Produção e migração

O build do frontend está em `Datagram WEB/Datagram-frontend/dist/primeiro-projeto/browser`.
Configure o servidor HTTP para servir `index.html` nas rotas do Angular e encaminhar
`/api/` ao backend **removendo esse prefixo**. Consulte os contratos e o roteiro de
migração antes de trocar um backend existente.

- [Relatório técnico da refatoração](docs/RELATORIO_REFATORACAO.md)
- [Contratos, configuração e migração](docs/API_E_MIGRACAO.md)

O diretório `Datagram ANDROID` é um cliente separado e não foi refatorado nem validado
nesta mudança. Consumidores antigos precisam ser adaptados aos contratos documentados.
