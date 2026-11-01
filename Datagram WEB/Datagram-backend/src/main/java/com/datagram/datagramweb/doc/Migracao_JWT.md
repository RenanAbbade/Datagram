# Autenticação

A documentação vigente está em `docs/API_E_MIGRACAO.md`, na raiz do repositório.
Login retorna `{token, usuario}`. Clientes devem enviar Bearer JWT. Não há segredo
fixo de produção no código, nem login comparando senhas em texto puro.
