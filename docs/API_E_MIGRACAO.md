# API, execução e migração

## Contratos da aplicação web

O navegador usa o prefixo `/api`. Os caminhos abaixo são os do Spring, depois de
remover o prefixo no proxy. Respostas de erro de validação/negócio usam `{message}`;
a segurança pode responder apenas com status 401/403. O frontend trata os dois casos.

| Método e caminho | Entrada | Resposta |
| --- | --- | --- |
| POST `/user/insert` | Register: nome, email, senha, dataNasc, tipoUsuario, campos de membro/pesquisador | 201 UserView, Location |
| POST `/user/login` | `{email,senha}` | 200 `{token,usuario}` |
| GET `/user/userLogado` | Bearer JWT | 200 UserView próprio |
| GET `/user/{id}` | Bearer JWT | 200 UserView; email somente do próprio usuário |
| PUT `/user` | Profile: nome, escolaridade, estado, municipio, instituicao, linkCv, interesses | 200 UserView próprio atualizado |
| POST `/user/foto` | multipart, campo `file` | 200 UserView próprio com URL versionada |
| GET `/user/{id}/foto` | sem JWT; foto de perfil pública | bytes PNG/JPEG/WebP |
| PUT/DELETE `/user/{id}/follow` | Bearer JWT | 200 perfil seguido/alvo atualizado |
| GET `/user/seguindo/{id}` | Bearer JWT | boolean |
| GET `/user/seguidores`, `/user/seguindo`, `/user/interesses` | Bearer JWT | UserView[] |
| GET `/user/nome?nome_value=...` | nome codificado como parâmetro | UserView[], até 50 na resposta |
| GET `/user/instituicao?instituicao_value=...` | instituição codificada como parâmetro | UserView[], até 50 na resposta |
| GET `/post/feed?page=0&size=20&autor=ID` | autor opcional; sem autor, próprios + seguidos | `{content:PostView[],hasNext:boolean}` |
| POST `/post/insert` | PostInput JSON ou multipart `post` (JSON) + `file` (PDF) | 201 PostView, Location |
| PUT `/post/{id}` | PostInput, somente autor | 200 PostView |
| PUT/DELETE `/post/{id}/like` | usuário obtido do JWT | 200 PostView, com `curtido` e total |
| DELETE `/post/{id}` | somente autor | 204, sem corpo |
| GET `/post/{id}/arquivo` | Bearer JWT | PDF, Content-Disposition attachment |
| GET `/post/maisCurtido` | Bearer JWT | até 10 posts próprios ordenados por curtidas |
| GET `/notificacao/` | Bearer JWT | até 50 publicações notificadas, mais recentes primeiro |
| GET `/gerente/mediaSeguidores`, `/gerente/maisConectados` | papel Gerente no servidor | número / UserView[] |
| POST `/municipios` | UF como texto, ex. SP | string[], IBGE com timeout e cache por UF |

PostInput: `titulo`, `subtitulo`, `conteudo`, `url`, `tipoPostagem` (`simples` ou
`cientifica`), `palavrasChave`. O cliente não determina autor, data, contadores ou
identidades de curtida. Os DTOs de leitura não retornam senha, CPF, listas internas
de IDs ou o Base64 de imagens/PDFs.

A foto é publicada na interface após o sucesso do servidor, não antes. A URL inclui
uma versão SHA-256 do conteúdo. Alterações feitas por outro dispositivo não geram
push nesta sessão: ainda não existe SSE, WebSocket ou polling periódico.

## Limites e autenticação

- Senha: 8–72 caracteres no cadastro; BCrypt, comparação sensível a maiúsculas.
- Cadastro público aceita apenas Membro e Pesquisador; Gerente deve ser provisionado
  por um procedimento administrativo confiável, fora desse endpoint.
- JWT: uma hora, enviado como `Authorization: Bearer ...` somente à API configurada.
- Token fica no sessionStorage da aba. Isso preserva a sessão no F5; continua
  acessível a JavaScript e não substitui controles contra XSS. Cookies HttpOnly
  exigiriam outro desenho de autenticação, CSRF e expiração.
- Logout remove token e estado local. Não há refresh token nem revogação antecipada
  do JWT já emitido; o servidor o aceita até a expiração.
- Imagens: PNG/JPEG/WebP, até 2 MiB; PDF: até 5 MiB. Cliente valida MIME/tamanho;
  servidor valida assinatura/tamanho. Isso não é antivírus nem análise completa
  de documentos. Imagens de perfil são públicas por desenho; PDFs exigem sessão.
- Feed: 20 itens por requisição, máximo 50. Paginação por deslocamento pode sofrer
  deslocamentos se houver inserções/exclusões simultâneas; não é cursor pagination.

## Configuração

Desenvolvimento usa H2 em memória, segredo JWT aleatório a cada processo e CORS
limitado a `localhost:4200` e `127.0.0.1:4200`. O JWT deixa de valer após reiniciar.
Não existe mais segredo fixo de produção no código.

Em produção, ative `SPRING_PROFILES_ACTIVE=prod` e forneça:

- `JWT_SECRET`: segredo aleatório estável de no mínimo 32 bytes, fora do Git.
- `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`.
- `CORS_ORIGIN`: origem(s) explícita(s), separadas por vírgula; sem wildcard.

O perfil prod usa `ddl-auto=validate`; não aplica alterações de esquema
silenciosamente. Inclua o driver JDBC do banco escolhido antes do deploy; esta
branch distribui H2, não configura automaticamente PostgreSQL/MySQL.

## Migração de uma instalação existente

Esta branch muda contratos. Não troque somente o frontend ou somente o backend.
Faça backup, use uma cópia de homologação e publique os dois em conjunto.

1. Revise consumidores externos, inclusive o Android. Remova dependência dos antigos
   textos `auth`, `manager`, `CREATED` e dos PUTs que misturavam edição e curtidas.
2. Revise e aplique uma migração de esquema compatível com seu banco: campos de
   versão otimista em Usuario/Postagem, IDs gerados por identidade no banco,
   tamanhos de coluna de conteúdo/URL/subtítulo/currículo, unicidade e
   obrigatoriedade do email.
   Preencha versões antigas com zero e resolva emails nulos/duplicados antes de
   ativar a restrição. Não foi executada migração em um banco real do usuário.
3. Normalize emails existentes para minúsculas e trate colisões antes de permitir
   novos cadastros. Normalize papéis legados para Membro/Pesquisador/Gerente.
4. Para senhas antigas em texto puro, há um runner **desativado por padrão**.
   Em janela de manutenção na cópia de homologação, ligue
   `MIGRATE_LEGACY_PASSWORDS=true`, valide o login e desligue a opção. O runner não
   registra senhas e não recodifica valores com formato BCrypt. Não há fallback
   de login aceitando texto puro.
5. Confira anexos antigos: fotos precisam estar em Data URL Base64 de PNG/JPEG/WebP;
   PDFs no formato usado pelo projeto. Corrija registros incompatíveis antes do
   corte. Datas novas usam Instant ISO-8601; textos antigos permanecem legíveis
   no frontend por fallback, sem migração cronológica automática.
6. Configure o proxy `/api`, fallback de rotas SPA e segredo estável. Exija novo login:
   sessões antigas não são compatíveis com o segredo/configuração corrigidos.
7. Execute a suíte em homologação e valide responsividade e conteúdo real com o dono
   do produto. Somente depois faça o merge/deploy de acordo com seu processo.

Os arquivos continuam armazenados internamente como texto Base64 no banco por
compatibilidade com o modelo existente. A otimização implementada é no contrato
HTTP: upload multipart e download separado, evitando repetir anexos grandes em
cada objeto JSON. Object storage/colunas binárias, thumbnails e CDN ficam para
uma migração específica de armazenamento.
