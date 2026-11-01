# Datagram web

Angular 22.2.4 standalone, formulários reativos estritamente tipados, estado com
Signals, navegação com layout persistente e carregamento sob demanda.

Use Node 24 (`nvm use`) e o backend Java 21 em localhost:8080.

```sh
npm ci
npm start
npm run build
npm test -- --watch=false
npm run format:check
```

Acesse http://localhost:4200. O proxy de desenvolvimento remove `/api` antes de
encaminhar a chamada ao Spring. O endereço da API fica nos environments.

```sh
npx playwright install --with-deps chromium
npm run test:e2e
```

O Playwright sobe ambas as aplicações e cria suas próprias contas de teste.
Compile o JAR do backend antes com `./mvnw verify`. Produção gera arquivos em
`dist/primeiro-projeto/browser`, exige fallback para index.html nas rotas Angular
e proxy `/api/` configurado no servidor.

Consulte [o relatório](../../docs/RELATORIO_REFATORACAO.md) e
[os contratos/migração](../../docs/API_E_MIGRACAO.md).
