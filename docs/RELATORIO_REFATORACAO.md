# Relatório de refatoração — Datagram

Branch de trabalho: **refatoracao-codex**. Base: commit `348734b`, publicado após a
migração para Angular 22.2.4. O trabalho não faz merge nem deploy da aplicação.

## 1. Diagnóstico: SPA e atualização de dados são problemas diferentes

A aplicação já era uma Single Page Application: existiam Router e router-outlet.
Entretanto, os componentes misturavam navegação tradicional, recargas completas,
DOM imperativo, consultas HTTP e cópias independentes dos mesmos dados.

Ser uma SPA não faz o Angular descobrir alterações no servidor. A tela apresenta
o estado que recebe. Se um componente guarda a foto antiga, salvar uma nova no
backend não altera essa cópia por conta própria. O F5 parecia corrigir o problema
porque destruía tudo e executava novamente as consultas iniciais.

No código anterior, o perfil e o cabeçalho consultavam o usuário separadamente.
Além disso, o PUT de atualização retornava 204 sem corpo, mas o componente
substituía o usuário pelo conteúdo vazio da resposta e chamava ngOnInit manualmente.
Portanto, havia inconsistência tanto no estado local quanto no contrato HTTP.

## 2. Iterações e decisões

### Iteração 1 — contratos e regras no backend

**Antes:** login esperava/produzia formatos incompatíveis entre Java e Angular;
validação de senha era comparação de texto ignorando maiúsculas; o filtro JWT
usava a entidade como principal, enquanto o serviço buscava por auth.getName().
Não havia garantia de que esse nome correspondesse ao email.

**Depois:** login é um contrato JSON `{token,usuario}`. A senha é persistida em
BCrypt e validada com PasswordEncoder. O principal autenticado tem como nome o
email validado pelo JWT. A chave de produção vem da configuração; em desenvolvimento,
a chave aleatória é descartada ao reiniciar. Credenciais incorretas retornam 401.

**Padrão aplicado:** contrato explícito, identidade autenticada consistente e
credencial com hash adaptativo. Isso torna verificável a identidade usada pelas
operações seguintes; não depende da interpretação de textos como "auth".

**Antes:** edição de usuário também servia para seguir alguém; edição de postagem
também servia para curtir, inferindo a intenção a partir de contadores recebidos.
Era possível enviar autor, ID de outra pessoa ou campos que não pertencem à edição.

**Depois:** DTOs diferentes para cadastro, login, perfil e postagem. Edição do perfil
sempre usa o usuário do JWT. Edição/exclusão de post verifica o autor no servidor.
Seguir e curtir têm endpoints PUT/DELETE próprios, usando a identidade autenticada.
Contadores e identidades não são aceitos do navegador como verdade.

**Padrão aplicado:** comandos com intenção explícita, autorização no servidor e
proteção contra alteração indevida de campos. PUT adiciona a relação; DELETE remove.
Repetir a mesma operação não duplica a curtida nem o seguidor: idempotência.

As operações relacionadas são transacionais. Campos @Version detectam conflitos
concorrentes e a API responde 409, em vez de sobrescrever silenciosamente um estado
mais recente. Isso não constitui um teste de carga nem uma prova de ausência de
todos os conflitos; define um comportamento seguro para conflitos detectados.

### Iteração 2 — estado, navegação e componentes no Angular

**Antes:** o mesmo cabeçalho era instanciado dentro de diferentes telas; havia
links href internos que solicitavam um novo documento, duas configurações
RouterModule.forRoot e desativação global de reaproveitamento de rotas.

**Depois:** um layout persistente contém cabeçalho e router-outlet. Rotas protegidas
ficam dentro desse layout; login e cadastro ficam fora. Há uma única definição de
rotas e páginas standalone carregadas sob demanda. Links internos usam routerLink.
Algumas URLs antigas com ID redirecionam para a rota canônica `/usuarios/:id`.

**Padrão aplicado:** composição por layout e roteamento declarativo. Navegação
altera o conteúdo da rota, sem recriar o documento nem o cabeçalho desnecessariamente.
O reaproveitamento deixa de ser combatido; o componente reage aos parâmetros.

**Antes:** usuário e foto existiam em diferentes variáveis locais, sem comunicação.

**Depois:** SessionService é a fonte compartilhada do usuário autenticado, exposta
por um Signal somente de leitura. Login, edição de perfil e upload confirmados
atualizam essa fonte. Perfil, cabeçalho e cartões de publicações próprias consomem
o mesmo estado. Não se faz previsão de sucesso antes de o servidor confirmar.

**Padrão aplicado:** fonte única de verdade no cliente e fluxo de dados explícito.
O backend continua sendo a autoridade da persistência; o Signal é uma representação
compartilhada da sessão, não um substituto para validação no servidor.

### Iteração 3 — interface declarativa, formulários e assincronismo

A revisão inicial encontrou 55 chamadas a document.getElementById, 24 usos de
JSON.parse(JSON.stringify(...)), seis chamadas manuais a ngOnInit e um template
de perfil com 907 linhas. Essas contagens descrevem a base anterior à refatoração.

**Antes:** manipulação de estilo/texto por ID; formulários aninhados e componentes
com várias responsabilidades; clone de JSON usado como substituto de tipagem.

**Depois:** formulários reativos tipados, controles e validações explícitos,
renderização condicional e classes declarativas. Perfil, formulário de perfil,
editor de publicação, cartão, avatar, sessão, HTTP e arquivos têm responsabilidades
separadas. Componentes de post/editor/avatar são reutilizados entre páginas.
TypeScript strict e strictTemplates estão habilitados.

**Padrões aplicados:** separação de responsabilidades, componentes coesos e modelo
de formulário explícito. Tipagem estática detecta incompatibilidades de código;
Bean Validation valida requisições reais. A tipagem do HttpClient não valida JSON
recebido em runtime; os contratos devem continuar sendo testados conjuntamente.

O frontend agora opera sem Zone.js. Signals notificam a atualização da tela, e a
validade dos formulários foi ligada a Signals por statusChanges. Os campos de
cadastro de membro/pesquisador têm validadores condicionais no modelo, em vez de
depender de controles que aparecem/desaparecem no DOM. Essa correção foi motivada
por um teste real que encontrou o botão de cadastro indevidamente desabilitado.

**Antes:** publicar iniciava o HTTP e imediatamente chamava location.reload(),
criando uma corrida entre o envio e a recarga. Erros quase não eram apresentados.

**Depois:** o serviço devolve um Observable, a tela espera o sucesso, atualiza o
estado ou navega por Router. Botões impedem envios concorrentes; falhas liberam
nova tentativa e mantêm o conteúdo do formulário/foto anterior. Há mensagens de
carregamento, erro e ausência de resultados.

Busca usa debounce e switchMap, cancelando consultas superadas. Mudanças de ID do
perfil usam switchMap; mudanças de UF também cancelam consultas antigas. Inscrições
de rota são encerradas com o ciclo de vida. Respostas de foto/follow não substituem
um perfil diferente caso o usuário tenha navegado enquanto a operação estava pendente.

**Padrão aplicado:** composição de operações assíncronas, controle de concorrência
na interface e atualização a partir do resultado confirmado. Não há necessidade
de chamar métodos de ciclo de vida manualmente.

### Iteração 4 — arquivos, consultas e tamanho do frontend

**Antes:** anexos Base64 e entidades completas eram transportados junto dos dados.
Três componentes repetiam conversão de PDFs e criavam URLs temporárias sem liberar.

**Depois:** upload multipart, validação de tamanho e assinatura no servidor e de
MIME/tamanho no cliente. UserView contém URL de foto versionada por SHA-256;
PostView informa se existe PDF, sem transportar o anexo. PDFs são baixados em
endpoint separado. O serviço de arquivos libera URLs temporárias após uso.
SVG não é aceito como foto. Cancelar a seleção não envia requisição.

**Padrão aplicado:** separação entre metadados e conteúdo binário e gestão explícita
de recursos. **O armazenamento interno ainda é Base64 no banco**: não foi implantado
object storage, CDN, antivírus ou geração de thumbnails.

**Antes:** o feed carregava listas e filtrava no Java repetidamente; havia comparação
de Integer por `==` e ordenação de datas de apresentação como texto. O ranking
buscava os usuários mais de uma vez e podia retornar 11 no lugar de 10.

**Depois:** feed filtrado por autor/seguidos no banco, página de 20 itens e máximo
50, ordenado por ID decrescente. Busca limitada no banco, sem diferenciar maiúsculas;
ranking top 10 e média por consulta; sugestões por interesses com limite de três;
notificações recentes por consulta e limite de 50. Exclusão remove notificações
associadas e ajusta o contador. Novas datas são definidas no servidor em ISO-8601.
IDs usam geração de identidade no banco.

**Padrões aplicados:** paginação, filtro próximo aos dados, ordenação determinística
e integridade transacional. Ainda há coleções JPA e consultas de associações que
podem ser refinadas com projeções/entity graphs após profiling. Não se afirma que
todos os problemas N+1 foram eliminados.

O frontend deixou de carregar Bootstrap 4, jQuery, Popper, Zone.js e templates de
dashboard duplicados. Um CSS pequeno e responsivo atende às telas. A interface foi
simplificada visualmente; não é uma reprodução pixel a pixel dos templates antigos.
Os budgets voltaram a ser restritos (400/600 kB inicial e 6/10 kB por componente).

### Iteração 5 — configuração, manutenção e validação

- Endereço da API configurado por environment; proxy local e instruções de proxy SPA
  em produção. Sem URL localhost hardcoded nos serviços de negócio do Angular.
- CORS centralizado e validado para origens explícitas. O teste de navegador detectou
  a diferença entre localhost e 127.0.0.1; ambos são autorizados somente em dev.
- Integração antiga de municípios substituída pela API oficial HTTPS do IBGE, com
  validação da UF, timeout e cache limitado às UFs. A interface permite digitação
  manual se o serviço externo estiver indisponível.
- Removidos Data REST e bibliotecas sem uso, evitando uma segunda superfície de
  acesso automático aos repositórios. A API usa os controllers definidos.
- Maven Wrapper restaurado com versão fixada; scripts de verificação e formatação.
- Removidos node_modules versionados e configurações antigas de Protractor/TSLint.
- Perfil de produção exige configuração e valida o esquema; não executa alteração
  silenciosa do banco. Há roteiro de migração e runner opt-in para senhas legadas.

## 3. Resultado medido de tamanho

Comparação entre builds de produção da base Angular 22 publicada e desta branch,
no mesmo ambiente. Valores decimais reportados pelo Angular CLI:

| Métrica | Antes | Depois | Redução |
| --- | ---: | ---: | ---: |
| Bundle inicial, tamanho bruto | 990,58 kB | 268,75 kB | 72,9% |
| Transferência inicial estimada pelo CLI | 195,94 kB | 73,27 kB | 62,6% |

As páginas agora têm chunks lazy adicionais. Esses números não representam o total
de todos os arquivos, nem medem tempo real de carregamento, LCP, consumo de CPU,
throughput do backend ou desempenho em aparelhos reais. O ganho de tamanho é
comprovado pelo build; os demais ganhos precisam de medições específicas.

## 4. Evidência de correção

A suíte verifica comportamentos, não apenas a compilação:

- API: autenticação verdadeira; senha ausente nas respostas e persistida com hash;
  rejeição de senha com capitalização diferente, JWT inválido, escalada de papel,
  edição/exclusão de post alheio e links inseguros.
- API: seguir/curtir repetidos sem duplicação, identidade correta, paginação, limpeza
  de notificações e contadores, upload de foto/PDF, download, limites de arquivo,
  origens CORS e migração explícita/idempotente de senhas legadas.
- Angular: sessão/contratos HTTP, token somente na API configurada, foto atualizada
  simultaneamente no perfil e cabeçalho, erro preservando a imagem anterior,
  cancelamento de seleção, validade reativa dos formulários e nova tentativa de envio.
- Angular: mudança de parâmetro no mesmo componente cancela requisições antigas.
- Navegador com backend real: cadastro, login, foto, perfil, publicação, curtida,
  edição, PDF, exclusão, busca, seguir, notificações, proteção de rotas e logout.
- Navegador: marca no objeto window preservada durante navegação/edição/publicação,
  comprovando ausência de recarga nesses fluxos; cenário móvel sem overflow horizontal.

A configuração de teste Mockito usa o mecanismo subclass: os testes atuais não
precisam instrumentar classes finais nem anexar agentes dinamicamente ao JDK 21.
A aplicação de produção não é afetada por essa configuração de testes.

Comandos e resultado final da rodada de validação estão na seção de entrega ao fim
deste relatório. O teste de navegador usa dados artificiais e H2, não contas reais.

## 5. Em que sentido a aplicação ficou mais correta

Nenhum framework ou padrão torna uma aplicação automaticamente correta. Aqui,
"mais correta" significa que as seguintes invariantes passaram a estar explícitas
no código e cobertas por testes:

1. Uma operação altera apenas o recurso que a identidade autenticada pode alterar.
2. O mesmo usuário aparece de forma consistente nas partes visíveis desta sessão.
3. A interface só anuncia persistência depois de uma resposta de sucesso.
4. Repetir seguir/curtir não aumenta indevidamente a contagem.
5. Navegar entre telas não exige destruir o documento para recuperar dados atuais.
6. Um erro não apaga o estado anterior nem impede uma nova tentativa válida.
7. Campos editáveis e respostas públicas são definidos por contratos, sem expor a
   entidade inteira ou aceitar contadores/autoria como autoridade do cliente.
8. A quantidade de posts carregada por consulta é limitada, com ordenação definida.

Os padrões usados são meios para manter essas invariantes: estado compartilhado,
UI declarativa, separação de responsabilidades, DTOs, autorização no servidor,
transações, controle otimista de concorrência, idempotência, paginação e testes de
regressão. Não é uma certificação de segurança, acessibilidade ou prontidão irrestrita
para produção.

## 6. Limites e próximos passos

Leia [API e migração](API_E_MIGRACAO.md) antes de substituir uma instalação existente.
Os contratos mudaram; o Android não foi adaptado nem testado. Também não houve
migração de um banco real, provisionamento de administrador, deploy ou merge.

Mudanças feitas em outro dispositivo não são recebidas automaticamente: o estado
reativo resolve os fluxos desta aba, não implementa sincronização por push.
Fotos são públicas e PDFs exigem autenticação. Token no sessionStorage e ausência
de revogação antecipada são escolhas documentadas, com limites explícitos.

Para uma próxima fase de produção: migrações SQL versionadas específicas do banco,
projeções JPA medidas por profiling, armazenamento de arquivos fora do banco,
limites de requisição/rate limiting, observabilidade, refresh/revogação de sessão,
acessibilidade com leitores de tela e testes em browsers/dispositivos adicionais.
Não foram introduzidos serviços externos ou infraestrutura sem necessidade para
validar a refatoração local.

## 7. Referências técnicas

- Angular Signals: https://angular.dev/guide/signals
- Reactive Forms: https://angular.dev/guide/forms/reactive-forms
- HTTP e contratos: https://angular.dev/guide/http/making-requests
- Guards e autorização: https://angular.dev/guide/routing/route-guards
- PasswordEncoder: https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/password-encoder.html
- API oficial de localidades: https://servicodados.ibge.gov.br/api/docs/localidades

## 8. Entrega e validação final

| Verificação | Resultado |
| --- | --- |
| Maven Wrapper, JDK 21, clean verify | BUILD SUCCESS; 12 testes, zero falhas/erros |
| Angular, build de produção | Sucesso; TypeScript strict e strictTemplates |
| Angular/Vitest | 12 testes passaram |
| Playwright/Chromium headless com backend real | 3 cenários passaram |
| Formatação Prettier | Todos os arquivos verificados passaram |

Os cenários de navegador incluem uma viewport de 390 × 844 e uma de desktop.
O navegador completo foi bloqueado pela política de sockets do ambiente; a
variante Chromium Headless Shell executou efetivamente os três cenários. Isso não
foi substituído por uma simulação de resultados.

O trabalho foi organizado em commits de contratos/backend, arquitetura/frontend,
correções de integração/consultas e documentação. O histórico da branch é a fonte
para os SHAs finais. A branch base não recebe essas alterações automaticamente.

Não houve medição de carga nem teste dos serviços externos do IBGE em produção.
Também não houve auditoria completa de dependências, teste do Android, migração
de dados do usuário, teste em Windows do mvnw.cmd ou deploy. A evidência corresponde
aos contratos e fluxos listados, no ambiente local com H2 e Chromium headless.
