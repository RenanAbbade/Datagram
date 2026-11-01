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
deste relatório. O teste de navegador usa dados artificiais e H2, não contas reais. A configuração
recusa servidores existentes e fixa um banco em memória para não reutilizar uma
instância ou credenciais reais por acidente.

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


# Parte II — Caderno técnico comentado

Esta parte é uma leitura de código para quem já programa, especialmente em Java,
mas não acompanhou profundamente Angular, TypeScript e programação reativa.
Não pressupõe familiaridade com Signals. O nome correto do recurso mencionado na
conversa é **Signals**; no singular, **Signal**.

**Escopo desta atualização:** somente documentação. Os exemplos reais se referem
à implementação existente na branch `refatoracao-codex`, cuja última alteração
funcional/testes antes deste texto está no commit `155eecd`. Exemplos marcados como
didáticos não são funcionalidades adicionais nem alterações executadas no produto.
Os resultados de testes da parte I são os da refatoração; não foram inventados
novos testes para uma edição de Markdown. O código Android não foi alterado.

## Roteiro de leitura

| Se você quer entender… | Leia |
| --- | --- |
| Por que a foto dependia do F5 | 9, 10, 11 e 12 |
| O que cada sintaxe do TypeScript significa | 10 e 28 |
| Signals, computed e effect | 11, 12 e 15 |
| Componentes, decorators, inputs e outputs | 13 e 14 |
| RxJS, HTTP, cancelamento e erros | 16, 17 e 18 |
| SPA, rotas, lazy loading e guards | 19 e 20 |
| Upload, cache e download de arquivo | 21 |
| DTOs, records e validação no Java | 22 e 23 |
| JWT, BCrypt, anotações e autorização | 24 e 25 |
| Transações, idempotência, versão e consultas | 26 e 27 |
| Como os testes comprovam o comportamento | 29 |
| Onde cada responsabilidade ficou e limites reais | 30 e 31 |

Os números abaixo dão continuidade ao relatório executivo. Trechos podem omitir
imports ou métodos vizinhos quando o foco é um conceito, mas a origem é indicada.

## 9. O problema original, visto pelo fluxo dos dados

### 9.1 SPA não significa sincronização automática

Uma SPA mantém o documento HTML principal e troca o conteúdo de partes da tela.
O Router decide qual componente ocupará uma região. Isso resolve a navegação;
não resolve sozinho a atualização dos dados de uma foto.

Imagine dois componentes com estas cópias independentes:

```typescript
// Exemplo didático do problema: referências e estados separados.
perfil.usuario = { id: 1, fotoPerfil: 'foto-antiga' };
cabecalho.usuarioLogado = { id: 1, fotoPerfil: 'foto-antiga' };

perfil.usuario = { id: 1, fotoPerfil: 'foto-nova' };
// Nada nesta atribuição altera cabecalho.usuarioLogado.
```

As duas variáveis representam a mesma pessoa, mas não têm um mecanismo que as
mantenha coerentes. O Angular não pode deduzir que uma resposta HTTP recebida pelo
perfil deve atualizar uma variável privada do cabeçalho.

### 9.2 O contrato agravava o problema


**Código anterior, base `348734b`:** `Datagram WEB/Datagram-frontend/src/app/perfil/perfil.component.ts`

```typescript
public updateUsuario(){

    this.usuarioService.updateUsuario(this.usuario).subscribe(res => {
      this.usuario = JSON.parse(JSON.stringify(res));
      this.ngOnInit();
    });
  }
```


**Código anterior, base `348734b`:** `Datagram WEB/Datagram-backend/src/main/java/com/datagram/datagramweb/Controller/UsuarioController.java`

```java
// UPDATE usuario logado
    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Usuario obj) {
        obj = service.update(obj);
        return ResponseEntity.noContent().build();
    }
```

O `subscribe` recebia `res`, mas o backend respondia `204 No Content`. O conteúdo
não era um usuário atualizado. Transformar a resposta em JSON não criava os dados
que faltavam: o componente podia atribuir `null` ao usuário e depois iniciar outra
consulta ao chamar `ngOnInit()`.

Isso misturava três decisões diferentes: salvar o perfil, reconstruir o estado e
reexecutar a inicialização. Além disso, não notificava necessariamente a cópia do
cabeçalho. O F5 escondia a incoerência porque começava tudo novamente.

### 9.3 O fluxo escolhido

Agora o upload tem uma sequência explícita:

1. A tela obtém o arquivo selecionado e valida requisitos básicos.
2. O `ApiService` envia `FormData` ao endpoint de foto.
3. O backend identifica o usuário pelo JWT, valida o arquivo e persiste a mudança.
4. O backend devolve `UserView`, já com a URL da foto atualizada.
5. A tela publica esse usuário no `SessionService`.
6. Cabeçalho, perfil e cartões próprios observam esse estado e passam a usar a foto.

A confirmação de persistência é o passo 4. Se ocorrer erro antes dele, a foto
anterior permanece no estado. É uma atualização **confirmada pelo servidor**, não
uma atualização otimista que exige desfazer a prévia em caso de falha.

## 10. TypeScript: o que os tipos resolvem e o que não resolvem

### 10.1 Interface e contrato de dados


**Código real da implementação:** `Datagram WEB/Datagram-frontend/src/app/core/models.ts`

```typescript
export interface User {
  id: number;
  nome: string;
  email: string | null;
  tipoUsuario: 'Membro' | 'Pesquisador' | 'Gerente';
  escolaridade: string | null;
  estado: string | null;
  municipio: string | null;
  instituicao: string | null;
  linkCv: string | null;
  fotoPerfil: string | null;
  interesses: string[];
  seguidores: number;
  seguindo: number;
  posts: number;
}
export interface ProfileInput {
  nome: string;
  escolaridade: string;
  estado: string;
  municipio: string;
  instituicao: string;
  linkCv: string;
  interesses: string[];
}
```

`interface User` descreve a forma que o código espera de um usuário. Não é uma
entidade JPA, não tem tabela e não existe como validador depois que TypeScript é
transformado em JavaScript. Seu papel é ajudar o compilador e o editor a verificar
os usos desse objeto.

Observe decisões concretas:

- `id: number`: neste contrato, o ID é numérico, não uma string arbitrária.
- `email: string | null`: o email pode estar ausente na representação pública.
  O tipo obriga o consumidor a considerar esse caso.
- `tipoUsuario: 'Membro' | 'Pesquisador' | 'Gerente'`: união de literais; um texto
  como `'Administrdor'` não é aceito onde esse tipo é exigido.
- `interesses: string[]`: uma lista de textos, não uma coleção sem forma conhecida.
- `ProfileInput` é diferente de `User`: editar perfil não dá ao navegador autoridade
  para editar ID, papel, senha ou contadores.

Há uma diferença entre **o objeto que uma tela lê** e **o comando que pode enviar**.
Essa separação evita reutilizar a mesma estrutura gigante para finalidades com
permissões diferentes. É semelhante à separação de request/response DTO no Java.

### 10.2 Por que reduzir `any`

```typescript
// Exemplo didático.
let semContrato: any = { nome: 'Renan' };
semContrato.nommee.toUpperCase(); // pode compilar e falhar na execução.

const comContrato: { nome: string } = { nome: 'Renan' };
// comContrato.nommee.toUpperCase(); // erro do compilador: propriedade inexistente.
```

Na aplicação anterior, converter repetidamente o objeto para JSON e de volta não
fornecia esse contrato. Também fazia trabalho desnecessário para dados que o
`HttpClient` já havia desserializado. O clone JSON ainda perderia informações de
tipos que não são JSON, se fosse aplicado a eles; por isso não é uma técnica geral
de modelagem nem de cópia segura.

### 10.3 Generics: `post<Session>` não é uma validação da resposta

```typescript
// Trecho real, com imports e restante do método omitidos.
this.http.post<Session>(`${environment.apiUrl}/user/login`, { email, senha });
```

`<Session>` informa ao TypeScript o formato esperado da resposta. O autocomplete
passa a conhecer `token` e `usuario`. O navegador, porém, ainda pode receber um JSON
incompatível se o servidor estiver errado. Essa é uma razão para existir teste de
integração do contrato; anotar um generic não valida magicamente a rede.

### 10.4 Null, acesso opcional e narrowing

```typescript
// Exemplo didático com a mesma modelagem da sessão.
const usuario: User | null = session.user();
const nome = usuario?.nome ?? 'Visitante';

if (usuario !== null) {
  console.log(usuario.nome); // dentro do if, o tipo já foi refinado para User.
}
```

`?.` interrompe o acesso se a referência for null/undefined. `??` usa o valor à
direita somente quando o anterior é null/undefined. Isso difere de `||`, que também
substitui valores falsy como string vazia e zero. No avatar, a string vazia também
não é uma URL útil, então o fallback com `||` é uma escolha coerente.

`unknown`, usado no tratamento de erro, faz o contrário de `any`: exige verificar
o tipo antes de acessar propriedades. A aplicação usa `instanceof HttpErrorResponse`
para reconhecer os erros HTTP e extrair uma mensagem adequada.

### 10.5 `private`, `readonly` e configuração estrita

`private current` comunica que a escrita do estado pertence ao serviço.
`readonly user` impede reatribuir essa propriedade pelo sistema de tipos; não
congela profundamente o usuário. Objetos/arrays dentro dele ainda podem ser mutáveis.
É uma disciplina de API interna, não uma barreira de segurança contra JavaScript
malicioso nem um equivalente automático a `Object.freeze` recursivo.

`strict` amplia as verificações de TypeScript; `strictTemplates` verifica também
expressões e bindings do HTML Angular. Isso detecta erros antes da execução, mas
não prova autorização, disponibilidade do servidor ou correção das regras de negócio.

## 11. Signals: estado observável sem um barramento global de eventos

### 11.1 O serviço real


**Código real da implementação:** `Datagram WEB/Datagram-frontend/src/app/core/session.service.ts`

```typescript
import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Session, User } from './models';
@Injectable({ providedIn: 'root' })
export class SessionService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private current = signal<User | null>(null);
  readonly user = this.current.asReadonly();
  readonly authenticated = computed(() => this.user() !== null);
  token(): string | null {
    return sessionStorage.getItem('datagram.token');
  }
  setUser(user: User) {
    this.current.set(user);
  }
  login(email: string, senha: string) {
    return this.http
      .post<Session>(`${environment.apiUrl}/user/login`, { email, senha })
      .pipe(
        tap((session) => {
          sessionStorage.setItem('datagram.token', session.token);
          this.setUser(session.usuario);
        }),
      );
  }
  restore() {
    return this.token()
      ? this.http.get<User>(`${environment.apiUrl}/user/userLogado`).pipe(
          tap((u) => this.setUser(u)),
          catchError(() => of(null)),
        )
      : of(null);
  }
  refresh() {
    return this.http
      .get<User>(`${environment.apiUrl}/user/userLogado`)
      .pipe(tap((u) => this.setUser(u)));
  }
  logout() {
    sessionStorage.removeItem('datagram.token');
    this.current.set(null);
    void this.router.navigateByUrl('/login');
  }
}
```

### 11.2 `signal<User | null>(null)`, passo a passo

- `signal(...)` cria um contêiner reativo para um valor atual.
- `User | null` indica que pode haver usuário autenticado ou nenhuma sessão carregada.
- O argumento `null` é o estado inicial, antes do login/restauração.
- `this.current()` **lê** o valor. Os parênteses não fazem uma chamada HTTP.
- `this.current.set(usuario)` **substitui** o valor e pode notificar consumidores.
- `this.current.update(anterior => novo)` calcula uma mudança a partir do valor anterior.

O Signal permite que o framework acompanhe quem leu aquele estado. No Datagram,
o cabeçalho lê `session.user()`; quando o upload publica um novo usuário, o
cabeçalho recebe uma dependência reativa que antes não existia.

### 11.3 Por que existe um Signal privado e outro exposto como leitura

```typescript
// Trecho real do SessionService.
private current = signal<User | null>(null);
readonly user = this.current.asReadonly();

setUser(user: User) {
  this.current.set(user);
}
```

Sem essa separação, qualquer componente poderia chamar `session.user.set(...)`
ou apagar a sessão de forma arbitrária. Com ela, a API pública estabelece um ponto
claro de alteração. `asReadonly()` não clona os dados nem transforma todo o grafo de
objetos em imutável: restringe a API de escrita do Signal exposto.

No código atual, `setUser` ainda é um método público, porque os fluxos de login,
perfil e foto precisam publicar uma resposta confirmada. Centralizar todo comando
numa facade de sessão seria um refinamento possível; não é uma capacidade que já
foi implementada e escondida no serviço.

### 11.4 Por que substituir um objeto é importante

```typescript
// Exemplo didático: evite mutar a referência exposta.
const atual = session.user();
if (atual) {
  // atual.fotoPerfil = 'nova'; // mutação direta não é a política de atualização.
  session.setUser({ ...atual, fotoPerfil: 'nova' });
}
```

O spread `...atual` copia os campos para um novo objeto e a propriedade seguinte
substitui somente a foto. A cópia é superficial. Por padrão, Signals usam igualdade
referencial para objetos; manter a mesma referência e mutar seu interior não é um
mecanismo confiável de notificação.

Na implementação do upload, nem é preciso fabricar esse objeto: o backend já
devolve a representação atualizada, e `setUser(u)` a publica.

### 11.5 Por que `computed` em vez de dois estados editáveis

```typescript
// Trecho real do SessionService.
readonly authenticated = computed(() => this.user() !== null);
```

`authenticated` é uma projeção de `user`. Não precisa de um segundo booleano que
possa esquecer de mudar no logout. O `computed` acompanha as leituras reativas da
sua função e mantém um valor derivado, avaliado sob demanda e reutilizado enquanto
suas dependências relevantes não mudarem.

No caso concreto: `user = null` implica `authenticated = false`. Um único estado
base evita a combinação incoerente "sem usuário, mas autenticado=true".

Isso não significa que ter um objeto na memória torna um JWT válido no servidor.
O guard organiza a navegação; o backend ainda valida o token em cada requisição.

### 11.6 Signal não substitui RxJS nem sincroniza dispositivos

Um Signal é adequado para perguntar **qual é o usuário agora?** Um Observable
modela uma sequência de eventos/valores ao longo do tempo, como mudanças de rota
ou o resultado de HTTP. Por isso os dois aparecem juntos.

Não há polling automático, WebSocket ou SSE dentro de `signal`. Mudar a foto em
outra aba/dispositivo não executa `setUser` nesta aba. O serviço também não é um
cache global entre navegadores: sua instância pertence à aplicação Angular atual.

A escolha de um serviço pequeno com Signals atende ao estado compartilhado desta
aplicação sem introduzir reducers/actions/store de uma biblioteca adicional.
NgRx pode ser útil em domínios com estados e efeitos muito mais complexos; não foi
necessário para corrigir a incoerência identificada aqui.

## 12. `@Injectable`, `inject()` e injeção de dependências

### 12.1 O decorator identifica o papel da classe

```typescript
// Trechos reais, extraídos da sessão.
@Injectable({ providedIn: 'root' })
export class SessionService {
  private http = inject(HttpClient);
  private router = inject(Router);
  // outros membros omitidos neste recorte
}
```

`@Injectable` é um decorator do Angular: fornece metadados para a infraestrutura
de injeção. `providedIn: 'root'` registra o serviço no injetor raiz da aplicação.
Os componentes que pedem esse serviço nesse escopo compartilham a instância.

Isso é crucial para a foto. Se cada componente declarasse novamente
`providers: [SessionService]`, poderia criar um escopo filho com outra instância
e recriar o problema de duas fontes de estado independentes.

`inject(HttpClient)` pede a dependência ao Angular, em vez de executar `new HttpClient`
no componente. O framework consegue fornecer a implementação e substituí-la no
ambiente de teste.

### 12.2 Comparação com a injeção no Spring

No Java, um construtor de serviço recebe repositórios e outros serviços. No Angular,
`inject(...)` durante a criação do objeto cumpre papel semelhante: declarar de que
infraestrutura a classe precisa. Isso é uma analogia de responsabilidade, não a
afirmação de que os dois contêineres têm o mesmo ciclo de vida.

O injetor Angular não é livremente acessível em qualquer função assíncrona.
`inject` precisa de contexto de injeção válido. Por isso ele aparece em campos,
construtores ou funções de framework como guards/interceptors, e não é chamado
arbitrariamente dentro de um callback de clique já em execução.

### 12.3 Por que não deixar HTTP espalhado em todos os componentes

A tela sabe que quer salvar uma foto. O `ApiService` sabe construir o endpoint,
o corpo multipart e o tipo de retorno. A sessão sabe compartilhar o usuário.
Cada parte pode ser alterada/testada sem copiar detalhes das demais.

Essa separação é uma aplicação de coesão e responsabilidade única. Ela não exige
que cada classe tenha um único método; exige que seus métodos tenham uma finalidade
relacionada e que a mudança tenha um lugar previsível.

## 13. `@Component` e standalone: uma unidade de interface explícita


**Código real da implementação:** `Datagram WEB/Datagram-frontend/src/app/shared/avatar.component.ts`

```typescript
import { Component, computed, input } from '@angular/core';
import { User } from '../core/models';
import { environment } from '../../environments/environment';
@Component({
  selector: 'app-avatar',
  template: `<img
    [src]="source()"
    [alt]="user()?.nome || 'Usuário'"
    width="48"
    height="48"
    loading="lazy"
  />`,
  styles: [
    `
      img {
        border-radius: 50%;
        object-fit: cover;
        background: #eee;
      }
    `,
  ],
})
export class AvatarComponent {
  user = input<User | null>(null);
  source = computed(
    () =>
      this.user()?.fotoPerfil?.replace(/^\/api(?=\/)/, environment.apiUrl) ||
      '/assets/img/usuario.png',
  );
}
```

### 13.1 O que cada campo significa

| Construção | Papel no avatar | Por que foi usada |
| --- | --- | --- |
| `@Component({...})` | Declara que a classe participa da renderização Angular | Liga comportamento, template e estilos |
| `selector: 'app-avatar'` | Permite escrever `<app-avatar>` em outras telas | Cria um componente reutilizável para a mesma responsabilidade |
| `template` | Define o HTML e seus bindings | A imagem segue o estado, sem buscar elementos por ID |
| `styles` | Define o estilo do componente | Aparência do avatar fica junto da sua implementação |
| `input<User \| null>(null)` | Recebe um usuário do componente pai | Avatar não precisa saber como fazer login ou buscar o perfil |
| `computed(...)` | Deriva a URL atual com fallback | Evita manter uma segunda variável de URL que possa ficar desatualizada |
| `[src]` | Vincula a propriedade src da imagem à expressão | O valor acompanha mudanças de `source()` |
| `[alt]` | Fornece um texto alternativo | A imagem possui descrição relacionada ao usuário |
| `loading="lazy"` | Solicita carregamento adiado ao navegador quando aplicável | Evita forçar todas as imagens fora da área visível imediatamente |

`@Component` é decorator. Já `input()`, `output()`, `signal()` e `computed()` são
funções/APIs; não são decorators somente porque participam do Angular.

### 13.2 Onde está o `standalone: true`?

Na versão Angular 22 usada aqui, os componentes novos são standalone por padrão;
não é necessário escrever a opção explicitamente em todos eles. O componente
declara suas dependências de template no próprio `imports` quando precisa delas.

Exemplo real de dependências do formulário:

```typescript
@Component({
  selector: 'app-profile-form',
  imports: [ReactiveFormsModule],
  // template omitido neste recorte
})
export class ProfileFormComponent {
  // membros omitidos
}
```

A mudança eliminou a obrigação de declarar todas as páginas num único AppModule.
A dependência fica próxima de quem a usa, o que também facilita separar páginas
em chunks de carregamento sob demanda.

Standalone não é uma regra de correção universal: uma aplicação com NgModules
bem organizados pode funcionar perfeitamente. Aqui ele simplificou uma estrutura
pequena que centralizava tudo e tinha configuração de rotas duplicada.

### 13.3 A expressão da URL

No avatar, a URL recebida começa com `/api`. O `replace` ajusta esse prefixo à
configuração do ambiente. O `|| '/assets/img/usuario.png'` oferece imagem padrão
quando não há foto. Não é uma consulta nem uma transformação de arquivo.

A URL versionada é criada no backend; a lógica de identidade do usuário também
não pertence ao avatar. Essa é a razão para esse componente continuar pequeno.

## 14. Inputs e outputs: o componente recebe dados e devolve eventos

Trechos reais do editor e formulário:

```typescript
// PostEditorComponent
post = input<Post | null>(null);
saved = output<Post>();

// ProfileFormComponent
user = input.required<User>();
saved = output<User>();
```

`input.required<User>()` declara que o consumidor deve fornecer o usuário. Ele não
faz HTTP para descobrir esse usuário. Como é uma entrada reativa, a leitura usa
`this.user()`. Ler um input obrigatório antes de ele ser fornecido é um erro de uso;
no formulário, a sincronização ocorre por `effect`, já no fluxo de criação da view.

`output<User>()` cria um canal de evento tipado do filho para o pai. Quando o
salvamento termina, `this.saved.emit(u)` comunica o resultado. O output não é um
Signal com valor atual e não é automaticamente um Observable RxJS.

Trecho real do template do perfil:

```html
<app-profile-form
  [user]="displayedUser()!"
  (saved)="user.set($event); editing.set(false)"
/>
```

- `[user]` envia dados do pai ao filho.
- `(saved)` escuta o evento do filho.
- `$event` é o usuário que o filho emitiu.
- `user.set($event)` atualiza o perfil apresentado pelo pai.
- `editing.set(false)` fecha o modo de edição somente após sucesso.
- O `!` é uma asserção não nula do TypeScript/template. Não faz validação runtime;
  neste ponto, o template já está protegido pela existência do usuário no bloco pai.

**Por que isso é melhor que o filho acessar o pai diretamente?** O formulário pode
ser reutilizado sem saber o nome da página que o contém. Ele informa "salvei este
usuário", e a página decide como apresentar esse resultado. Isso reduz acoplamento.

Também há diferença entre evento local e estado global da sessão. O evento fecha
o editor daquele pai; `session.setUser(u)` atualiza cabeçalho e demais consumidores.
Um não substitui automaticamente o outro.

## 15. `effect`, formulários reativos e a ponte com Signals

### 15.1 O formulário real


**Código real da implementação:** `Datagram WEB/Datagram-frontend/src/app/pages/profile-form.component.ts`

```typescript
form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    escolaridade: [''],
    estado: [''],
    municipio: [''],
    instituicao: [''],
    linkCv: [''],
    tags: [''],
  });
  formStatus = toSignal(this.form.statusChanges, {
    initialValue: this.form.status,
  });
  constructor() {
    effect(() => {
      const u = this.user();
      this.form.patchValue(
        {
          nome: u.nome,
          escolaridade: u.escolaridade || '',
          estado: u.estado || '',
          municipio: u.municipio || '',
          instituicao: u.instituicao || '',
          linkCv: u.linkCv || '',
          tags: u.interesses.join(', '),
        },
        { emitEvent: false },
      );
      this.form.updateValueAndValidity();
    });
    this.form.controls.estado.valueChanges
      .pipe(
        tap(() => {
          this.cities.set([]);
          this.form.controls.municipio.setValue('');
        }),
        switchMap((uf) =>
          uf
            ? this.api.municipalities(uf).pipe(
                catchError(() => {
                  this.error.set(
                    'Não foi possível listar municípios. Você pode digitar o nome.',
                  );
                  return of([] as string[]);
                }),
              )
            : of([] as string[]),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((cities) => this.cities.set(cities));
  }
```

### 15.2 FormGroup, FormControl e `nonNullable`

`group(...)` cria um grupo de controles com nomes conhecidos. `nome` não é apenas
uma string HTML: é um controle com valor, validade, erros e histórico de interação.
`Validators.required` e `Validators.maxLength(120)` são funções de validação desse
controle.

`nonNullable` evita que os controles sejam inferidos como `string | null` por causa
do comportamento padrão de reset. Isso reduz verificações artificiais em valores
que, neste formulário, devem ser strings mesmo quando vazios. Não torna os campos
obrigatórios: essa responsabilidade é de validadores como `required`.

Exemplo didático equivalente de uso:

```typescript
const nome = form.controls.nome.value; // string neste grupo nonNullable.
form.controls.nome.setValue('Renan');
// form.controls.nome.setValue(123); // incompatível com o tipo string.
```

### 15.3 Por que `effect` aparece na inicialização do editor

O editor recebe `user` por input e precisa preencher controles de uma API imperativa
(FormGroup). O `effect` acompanha esse input e sincroniza seus campos por `patchValue`.
É uma fronteira apropriada: transformar uma mudança reativa num comando para o
modelo de formulário.

Não usei um `effect` para calcular `authenticated` ou a URL do avatar. Esses são
valores derivados e pertencem a `computed`. Copiar valores derivados entre Signals
por efeitos aumenta a chance de ciclos e fontes de verdade duplicadas.

`patchValue` atualiza os campos informados. `setValue`, por contraste, exige a forma
completa do grupo. Na edição do perfil, interessa mapear apenas os campos editáveis,
não ID, papel e contadores.

### 15.4 Por que `emitEvent: false`

A inicialização preenche também `estado`. O mesmo controle dispara a consulta de
municípios quando o usuário muda a UF. Se o preenchimento inicial emitisse como uma
nova escolha do usuário, o fluxo poderia apagar o município já salvo.

`emitEvent: false` evita essa cascata durante o preenchimento. Em seguida,
`updateValueAndValidity()` recalcula/publica a validade do grupo. A intenção está
separada: preencher um perfil existente não é a mesma coisa que selecionar outra UF.

Um limite real: se o pai fornecer outro objeto de usuário enquanto a pessoa edita,
o efeito pode preencher novamente o formulário. Não há nesta versão um mecanismo
completo de merge de rascunho ou resolução de alterações simultâneas de formulário.

### 15.5 Por que `toSignal(statusChanges)` foi necessário

```typescript
// Trecho real.
formStatus = toSignal(this.form.statusChanges, {
  initialValue: this.form.status,
});
```

`statusChanges` é um Observable do formulário. `toSignal` faz a ponte para um valor
reativo que o template pode ler. `initialValue` fornece o estado antes de qualquer
emissão; assim o botão começa refletindo a validade inicial.

```html
<!-- Trecho real do formulário. -->
<button type="submit" [disabled]="formStatus() !== 'VALID' || busy()">
  {{ busy() ? 'Salvando…' : 'Salvar perfil' }}
</button>
```

Se o formulário estiver `INVALID`, `PENDING` ou `DISABLED`, não há envio por esse
botão. `busy()` evita outro envio durante a requisição. A mesma proteção também
existe no método; desabilitar um botão não deve ser a única checagem de fluxo.

Na aplicação sem Zone.js, não se deve presumir que ler uma propriedade arbitrária
como `form.invalid` seja por si só uma notificação reativa para a view. O teste real
identificou a necessidade de refletir o estado do formulário explicitamente.
`toSignal` cria uma assinatura, então deve ser criado uma vez e reutilizado, não
recriado a cada leitura de template.

### 15.6 Validadores condicionais no cadastro


**Código real da implementação:** `Datagram WEB/Datagram-frontend/src/app/pages/register.component.ts`

```typescript
constructor() {
    const configure = (type: string) => {
      this.form.controls.cpf.setValidators(
        type === 'Membro' ? [Validators.required] : [],
      );
      for (const control of [
        this.form.controls.instituicao,
        this.form.controls.dataInicio,
      ]) {
        control.setValidators(
          type === 'Pesquisador' ? [Validators.required] : [],
        );
        control.updateValueAndValidity({ emitEvent: false });
      }
      this.form.controls.cpf.updateValueAndValidity({ emitEvent: false });
      this.form.updateValueAndValidity();
    };
    configure(this.form.controls.tipoUsuario.value);
    this.form.controls.tipoUsuario.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe(configure);
  }
```

O cadastro tem regras diferentes para Membro e Pesquisador. Esconder o campo CPF
não torna correto exigir CPF de um pesquisador no modelo que continua existindo.
Por isso a alteração de tipo muda os validadores dos controles correspondentes.

`setValidators(...)` redefine a regra daquele controle. `updateValueAndValidity`
aplica a regra ao valor atual. Os controles intermediários usam `emitEvent: false`
e o grupo é atualizado ao final, para evitar uma sequência desnecessária de eventos.

Isso também explica por que o refactor não foi apenas trocar sintaxe: um teste
reproduziu o formulário de pesquisador indevidamente inválido. A correção foi
colocar a regra no modelo, com um teste que verifica o botão e o grupo.

### 15.7 `effect` e `toSignal` não são recursos para usar em toda variável

Formulários temporários pertencem ao editor; sessão pertence ao serviço de sessão;
valores derivados pertencem a `computed`; eventos assíncronos continuam em RxJS.
A fronteira foi escolhida pela responsabilidade do dado, não por obrigação de
substituir todas as APIs existentes por Signals.


## 16. Observable e RxJS: modelar eventos, não espalhar callbacks

### 16.1 O serviço HTTP devolve a operação

Trecho real do ApiService:

```typescript
profile(data: ProfileInput) {
  return this.http.put<User>(`${this.base}/user`, data);
}

photo(file: File) {
  const body = new FormData();
  body.append('file', file);
  return this.http.post<User>(`${this.base}/user/foto`, body);
}
```

O serviço monta a requisição e retorna o Observable. Ele não navega, não abre
alertas e não fecha o formulário. A tela pode compor a operação e decidir o que
fazer em sucesso/erro.

No HttpClient, a assinatura dispara a requisição. Guardar o Observable numa
variável não equivale a já ter executado a operação. Assinaturas independentes no
mesmo Observable HTTP também podem disparar requisições independentes; esse retorno
não é um cache automático.

Essa separação corrige o padrão antigo de o serviço chamar subscribe internamente
e o componente fazer reload sem poder aguardar o resultado. Um método que retorna
Subscription não oferece ao chamador a mesma possibilidade de compor o resultado
como um método que retorna Observable de dados.

### 16.2 Leitura do salvamento, operador por operador


**Código real da implementação:** `Datagram WEB/Datagram-frontend/src/app/pages/profile-form.component.ts`

```typescript
save() {
    if (this.form.invalid || this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    const { tags, ...d } = this.form.getRawValue();
    this.api
      .profile({
        ...d,
        interesses: tags
          .split(',')
          .map((t) => t.trim())
          .filter(Boolean),
      })
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: (u) => {
          this.session.setUser(u);
          this.saved.emit(u);
        },
        error: (e) => this.error.set(errorMessage(e)),
      });
  }
}
```

O recorte inclui o fechamento da classe. A sequência do método é:

1. `if (this.form.invalid || this.busy()) return`: evita envio inválido/repetido.
2. `busy.set(true)`: apresenta que há uma operação em andamento.
3. `error.set('')`: remove a mensagem antiga para esta tentativa.
4. `getRawValue()`: obtém os valores do formulário, inclusive controles desabilitados
   se houver. Isso não significa que campos arbitrários do usuário serão enviados:
   o grupo já define os campos que existem.
5. `{ tags, ...d }`: separa o texto de tags dos demais campos editáveis.
6. `split/map/filter`: transforma o texto numa lista limpa de interesses.
7. `api.profile(...)`: devolve a operação de salvar.
8. `pipe(finalize(...))`: estabelece a limpeza do estado de carregamento.
9. `subscribe({ next, error })`: executa e trata os resultados da operação.
10. `setUser` e `saved.emit`: atualizam a sessão e notificam o pai somente no sucesso.

`finalize` roda quando a assinatura termina por sucesso, erro ou cancelamento.
Ele não indica que o servidor salvou: a confirmação de sucesso está em `next`.
Por isso a atualização de usuário não fica dentro de `finalize`.

`subscribe` sem tratamento de erro não cria uma estratégia de recuperação por si
só. Aqui a mensagem é mostrada e o estado busy é liberado, permitindo outra tentativa.

### 16.3 `tap`, `map` e `subscribe` têm funções diferentes

| Recurso | Função | Exemplo no Datagram |
| --- | --- | --- |
| `map` | Transforma cada valor emitido | Converter a busca em texto sem espaços externos |
| `tap` | Executa uma ação sem substituir aquele valor da sequência | Guardar token e publicar usuário após login |
| `subscribe` | Conecta o consumidor à execução e recebe resultados | Aplicar o retorno de salvamento à tela |
| `finalize` | Faz limpeza ao terminar a assinatura | Liberar o botão mesmo após falha |
| `catchError` | Decide o fluxo após um erro | Mostrar mensagem e continuar permitindo novas buscas |

Uma diferença comum para quem vem de Java: o `map` de RxJS transforma emissões ao
longo do tempo; o `map` de um array transforma elementos daquela coleção; o
`Stream.map` de Java transforma elementos de um stream Java. Os nomes são parecidos,
mas são abstrações e ciclos de execução diferentes.

### 16.4 `catchError(() => of(null))` não quer dizer sucesso

Na restauração da sessão, o código converte falha em um resultado nulo para a
inicialização não ficar bloqueada por uma exceção de rede. Isso tem um custo de
expressividade: nulo não distingue, sozinho, "sem sessão" de "servidor indisponível".
O interceptor encerra a sessão quando recebe 401 do token corrente; outros erros
podem levar à tela sem usuário carregado. Um estado mais rico de restauração seria
uma melhoria futura de UX.

É importante documentar essa limitação em vez de dizer que qualquer erro deve ser
silenciosamente convertido em sucesso. Em outros fluxos, como salvar perfil, o erro
é apresentado e o usuário anterior é preservado.

## 17. `switchMap`, `forkJoin` e ciclo de vida

### 17.1 Por que trocar de ID pode exibir a pessoa errada

Na rota `/usuarios/1`, é possível navegar para `/usuarios/2` sem reconstruir a
mesma classe de página. Reagir apenas ao primeiro `ngOnInit` não é suficiente.

Há também uma corrida: a consulta de 1 pode demorar e chegar depois da consulta de
2. Se qualquer resposta puder sobrescrever o estado, o título da pessoa 1 pode
aparecer quando a URL já aponta para 2.


**Código real da implementação:** `Datagram WEB/Datagram-frontend/src/app/pages/profile.component.ts`

```typescript
ngOnInit() {
    this.route.paramMap
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.error.set('');
          this.editing.set(false);
          this.peopleTitle.set('');
          this.user.set(null);
          this.posts.set([]);
        }),
        switchMap((params) => {
          const id = Number(params.get('id') || this.session.user()?.id);
          return forkJoin({
            user: this.api.user(id),
            posts: this.api.posts(0, id),
            following:
              id === this.session.user()?.id
                ? of(false)
                : this.api.following(id),
          }).pipe(
            catchError((e) => {
              this.error.set(errorMessage(e));
              this.loading.set(false);
              return of(null);
            }),
          );
        }),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe((result) => {
        this.loading.set(false);
        if (result) {
          this.user.set(result.user);
          this.posts.set(result.posts.content);
          this.hasNext.set(result.posts.hasNext);
          this.following.set(result.following);
          this.page = 1;
        }
      });
  }
```

### 17.2 O que o pipeline faz

- `paramMap` emite quando os parâmetros da rota mudam.
- O primeiro `tap` coloca a página em carregamento e limpa o estado de apresentação
  da rota anterior. Limpar esses dados não é recarregar o documento.
- `switchMap` troca a assinatura interna: ao chegar outro ID, deixa de observar a
  operação do ID anterior e passa a observar a nova.
- `forkJoin` combina as consultas de usuário, posts e relação de seguir. Como as
  consultas HTTP emitem e completam, o resultado combinado chega quando todas
  tiverem completado com sucesso.
- `of(false)` substitui a consulta de relação quando o perfil é do próprio usuário.
- O `subscribe` recebe um objeto com as três respostas já associadas à mesma rota.

Cancelar uma assinatura HTTP pode abortar a requisição no transporte, mas **não é
rollback de trabalho já executado no servidor**. É por isso que `switchMap` é muito
apropriado para leituras que ficaram obsoletas, e não deve ser aplicado sem reflexão
para encadear gravações que precisam ocorrer todas.

`forkJoin` também não é uma transação distribuída: se uma leitura falhar, o grupo
falha, mas não desfaz qualquer efeito de outro endpoint. Aqui ele agrupa leituras.

### 17.3 Por que o `catchError` está dentro de `switchMap`

Nesse ponto, uma falha pertence à consulta de um ID. Ela vira `null` e uma mensagem
na tela, mas a assinatura externa continua podendo receber o próximo parâmetro.
Se o tratamento encerrasse a sequência externa, uma navegação posterior poderia
não iniciar outra consulta no mesmo componente.

### 17.4 O que `takeUntilDestroyed` resolve

A inscrição em parâmetros de rota não tem a mesma duração de um único HTTP. Ela
pode continuar emitindo durante a vida da página. `takeUntilDestroyed` encerra a
inscrição quando o contexto do componente é destruído.

No `ngOnInit`, o código passa `this.destroy` explicitamente. No construtor, a API
pode obter o contexto de injeção corrente. Esse detalhe evita copiar uma chamada
sem argumento para um lugar que não possui contexto de injeção.

Nem todo subscribe é automaticamente um vazamento: um HTTP normalmente completa.
Ainda assim, encerrar leituras superadas evita trabalho e respostas sem utilidade.
A justificativa deve considerar a duração da fonte, não aplicar mecanicamente uma
regra de que toda assinatura é eterna.

### 17.5 Por que ainda existe uma checagem de ID no retorno do upload/follow

```typescript
// Trecho real do retorno de photo().
this.session.setUser(u);
if (this.user()?.id === u.id) this.user.set(u);
```

A sessão precisa receber a nova foto própria mesmo que a pessoa tenha navegado.
A página, porém, só deve receber esse usuário se ainda apresenta o mesmo ID.
Isso impede que um resultado de upload substitua um perfil diferente que foi aberto
enquanto a gravação estava pendente.

Essa defesa não torna todos os fluxos imunes a qualquer corrida. Por exemplo,
algumas listas auxiliares ainda poderiam receber controle adicional de validade
por rota; paginação por deslocamento também tem limites sob alterações simultâneas.
O relatório distingue as defesas implementadas de garantias universais.

## 18. A busca como exemplo de pipeline bem delimitado


**Código real da implementação:** `Datagram WEB/Datagram-frontend/src/app/pages/search.component.ts`

```typescript
constructor() {
    this.query.valueChanges
      .pipe(
        map((q) => q.trim()),
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((q) => {
          this.error.set('');
          this.searched.set(q.length >= 2);
          this.loading.set(q.length >= 2);
          if (q.length < 2) return of([] as User[]);
          return forkJoin([
            this.api.search(q, 'nome'),
            this.api.search(q, 'instituicao'),
          ]).pipe(
            map((lists) =>
              Array.from(new Map(lists.flat().map((u) => [u.id, u])).values()),
            ),
            catchError((e) => {
              this.error.set(errorMessage(e));
              return of([] as User[]);
            }),
          );
        }),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe((u) => {
        this.users.set(u);
        this.loading.set(false);
      });
  }
}
```

Leia a busca na ordem dos operadores:

1. `trim` evita pesquisar espaços externos sem significado para esta interface.
2. `debounceTime(300)` aguarda um intervalo de 300 ms sem novas emissões. Reduz a
   quantidade de consultas enquanto a pessoa ainda digita; não cria cache.
3. `distinctUntilChanged()` ignora uma repetição consecutiva do mesmo texto já
   normalizado. Não elimina todo valor que já apareceu alguma vez no histórico.
4. O limite de dois caracteres evita consultas amplas para uma letra ou texto vazio.
5. `switchMap` troca a operação quando um novo texto passa pelos operadores anteriores.
6. `forkJoin` consulta nome e instituição em paralelo, esperando os dois resultados.
7. `new Map(...id...)` combina os resultados sem repetir o mesmo usuário por ID.
8. O tratamento interno de erro deixa a busca continuar funcionando numa próxima entrada.

Há uma nuance de tempo: como o debounce vem **antes** do switchMap, a troca da
consulta anterior só acontece quando o novo texto atravessa o debounce. Não é
correto afirmar que cada tecla cancela imediatamente toda leitura anterior.
Durante esse intervalo, um resultado anterior ainda pode chegar. O comportamento
implementado reduz requisições e troca a consulta na emissão do texto estabilizado.

**Por que não duas assinaturas independentes no componente?** Com dois callbacks
soltos, seria necessário coordenar manualmente o fim das consultas, eliminar
repetições e evitar respostas de textos diferentes misturadas na mesma lista.
O pipeline torna essas decisões visíveis e testáveis num único fluxo.

## 19. Router, layout persistente e carregamento sob demanda

### 19.1 Uma única árvore de rotas


**Código real da implementação:** `Datagram WEB/Datagram-frontend/src/app/app.routes.ts`

```typescript
export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'cadastro',
    loadComponent: () =>
      import('./pages/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout.component').then((m) => m.LayoutComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'feed' },
      {
        path: 'feed',
        loadComponent: () =>
          import('./pages/feed.component').then((m) => m.FeedComponent),
      },
      {
        path: 'perfil',
        loadComponent: () =>
          import('./pages/profile.component').then((m) => m.ProfileComponent),
      },
      {
        path: 'usuarios/:id',
        loadComponent: () =>
          import('./pages/profile.component').then((m) => m.ProfileComponent),
      },
```

O trecho foi interrompido no meio da lista para destacar a árvore; as demais rotas
estão no mesmo arquivo. O bloco pai carrega `LayoutComponent` e contém as páginas
internas. Login e cadastro estão fora desse layout.

`children` significa que a página filha ocupará o outlet do layout. O cabeçalho
permanece na árvore enquanto a pessoa navega entre os filhos desse layout. Isso
não impede que o layout seja destruído ao sair para a tela de login.

`pathMatch: 'full'` exige que a rota vazia corresponda ao caminho todo para executar
aquele redirecionamento. Sem distinguir correspondência completa de prefixo,
um redirect pode ser aplicado a caminhos que não se pretendia redirecionar.

### 19.2 O que `loadComponent` e `import()` fazem

```typescript
// Trecho real da rota do perfil.
{
  path: 'perfil',
  loadComponent: () =>
    import('./pages/profile.component').then((m) => m.ProfileComponent),
}
```

`import()` é importação dinâmica e devolve uma Promise do módulo. O callback
seleciona o componente exportado. O bundler pode separar esse código num chunk,
para o Router carregá-lo quando essa página for necessária.

Isso reduz o JavaScript inicial. Não reduz necessariamente a soma de todos os
arquivos nem remove o custo quando a página for aberta. Por isso o relatório mede
bundle **inicial**, sem chamar esse número de tamanho total do sistema.

### 19.3 `routerLink` em vez de uma nova navegação de documento

```html
<!-- Exemplo didático do padrão anterior. -->
<a href="/perfil">Minha conta</a>

<!-- Padrão usado no layout novo. -->
<a routerLink="/perfil">Meu perfil</a>
```

O primeiro solicita navegação tradicional para aquele endereço. O segundo integra
o clique ao Router. O navegador mantém o documento e o Angular altera a rota.
Links externos continuam usando href, pois não pertencem ao roteamento interno.

A aplicação ainda precisa de configuração correta no servidor HTTP: abrir
`/usuarios/2` diretamente deve servir index.html, e não resultar em 404 do servidor
de arquivos. Isso é o fallback SPA documentado; o Router não pode configurar o
servidor de hospedagem sozinho.

## 20. Guards, interceptors e a fronteira de segurança


**Código real da implementação:** `Datagram WEB/Datagram-frontend/src/app/core/auth.ts`

```typescript
import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { SessionService } from './session.service';
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);
  if (!req.url.startsWith(environment.apiUrl + '/')) return next(req);
  const token = session.token();
  const request = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;
  return next(request).pipe(
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        token &&
        session.token() === token
      )
        session.logout();
      return throwError(() => error);
    }),
  );
};
export const authGuard: CanActivateFn = () =>
  inject(SessionService).authenticated() ||
  inject(Router).createUrlTree(['/login']);
export const adminGuard: CanActivateFn = () =>
  inject(SessionService).user()?.tipoUsuario === 'Gerente' ||
  inject(Router).createUrlTree(['/feed']);
```

### 20.1 Guard é navegação, não autorização definitiva

O guard devolve `true` quando há sessão carregada ou uma `UrlTree` para o login.
A `UrlTree` descreve o redirecionamento ao Router. O guard de gerente usa o papel
carregado na sessão para evitar apresentar a página a quem não possui esse papel.

Esse JavaScript está no computador do usuário e pode ser modificado. Logo, ele
não é prova de permissão para o backend. O endpoint `/gerente/**` também exige o
papel no Spring Security; editar/excluir posts verifica autoria no serviço Java.

### 20.2 Por que um interceptor

Sem interceptor, cada serviço precisaria copiar a construção do Authorization e o
tratamento de expiração. A função centraliza esse comportamento de transporte.
`HttpInterceptorFn` é o tipo da função reconhecida pelo Angular, não um decorator.

`req.clone(...)` cria uma requisição com os headers desejados, preservando o modelo
imutável da requisição que está atravessando a cadeia. `next(request)` passa para
o próximo passo da cadeia e, finalmente, o transporte.

O prefixo da API é conferido antes de anexar o token. Isso evita enviá-lo também
numa chamada a outro host só porque a aplicação tem sessão. A defesa foi testada
com uma URL externa artificial. A verificação depende de configurar `apiUrl`
corretamente; não é uma política independente de qualquer configuração.

### 20.3 Por que comparar o token que falhou com o token atual

Uma resposta 401 de uma requisição antiga pode chegar depois de um novo login.
`session.token() === token` evita encerrar a sessão nova por causa da falha de um
token que já foi substituído. É um exemplo pequeno de proteção contra resposta
assíncrona fora de ordem.

`throwError(() => error)` propaga a falha para o consumidor depois do tratamento
da sessão. Se o interceptor convertesse todos os erros em sucesso, a tela poderia
apagar a mensagem ou interpretar que um salvamento terminou corretamente.

### 20.4 Por que não foi colocado JWT em todo lugar do template

Autenticação é responsabilidade da sessão e do transporte HTTP. Colocar tokens
em URLs de imagens, campos ocultos ou parâmetros de links espalharia informação
sensível por histórico, logs e componentes. A API de fotos adotada nesta versão
é pública, e o download de PDF autenticado usa HttpClient com o interceptor.
Essa escolha é documentada; fotos privadas exigiriam outro fluxo.

## 21. Upload, versão da foto e liberação de recursos

### 21.1 Multipart tem um papel diferente de JSON

```typescript
// Trecho real do ApiService.photo().
const body = new FormData();
body.append('file', file);
return this.http.post<User>(`${this.base}/user/foto`, body);
```

`File` representa o arquivo recebido do navegador. `FormData` prepara campos de
um formulário multipart, permitindo enviar os bytes como arquivo. O navegador
monta o Content-Type e seu boundary; não se deve inserir manualmente apenas
`multipart/form-data` esquecendo o boundary.

O nome `file` precisa coincidir com o nome esperado pelo backend:


**Código real da implementação:** `Datagram WEB/Datagram-backend/src/main/java/com/datagram/datagramweb/Controller/UsuarioController.java`

```java
@PostMapping(value = "/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public UserView photo(@RequestParam("file") MultipartFile file) throws java.io.IOException {
    String data = com.datagram.datagramweb.api.Uploads.image(file);
    Usuario u = users.authenticated();
    u.setFotoPerfil(data);
    photoRepo.save(u);
    return UserView.of(u, true);
  }
```

- `@PostMapping` associa o método ao verbo/caminho e, aqui, restringe o formato do corpo.
- `@RequestParam("file")` extrai a parte multipart com esse nome.
- `MultipartFile` é a abstração recebida no servidor; não é o File do navegador.
- `Uploads.image` valida tamanho e assinatura do formato admitido.
- `users.authenticated()` define de quem é a foto. Não há confiança num ID enviado
  pelo navegador para escolher o dono do upload.
- `UserView.of(u, true)` devolve os metadados necessários para atualizar a interface.

A validação do cliente melhora a experiência, mas pode ser burlada. O servidor
precisa repetir limites relevantes. A validação atual não é antivírus nem uma
garantia de decodificação completa de qualquer imagem/documento.

### 21.2 Por que a URL recebe uma versão do conteúdo

```java
// Trechos reais de ApiDtos, com a chamada de hash abreviada neste recorte.
String photo = u.getFotoPerfil() == null
    ? null
    : "/api/user/" + u.getId() + "/foto?v=" + photoVersion(u.getFotoPerfil());
```

`photoVersion` usa SHA-256 para representar a versão do conteúdo. Quando o conteúdo
muda, a URL normalmente muda também. Isso diferencia a nova imagem para o navegador
sem acrescentar um timestamp aleatório em toda renderização.

SHA-256 aqui **não criptografa a foto**, não autentica o usuário e não é a técnica
usada para guardar senhas. Ele identifica a versão do conteúdo. BCrypt cumpre uma
responsabilidade de segurança diferente.

O endpoint também envia `Cache-Control: no-cache`. Esse valor exige revalidação
antes de reutilizar uma resposta armazenada; não equivale semanticamente a
`no-store`, que proíbe seu armazenamento. Não foi implementada uma estratégia
completa de ETag/304/CDN nesta mudança.

### 21.3 Por que separar PDF da resposta do feed

Uma lista de posts precisa, em geral, de título, autor e texto. Transportar um PDF
Base64 inteiro em cada listagem aumenta o payload mesmo quando ninguém o abre.
O `PostView` devolve `temArquivo`, e os bytes só são solicitados quando necessários.

A mesma ideia vale para a foto: o JSON passa a ter uma URL, não toda a imagem.
Isso otimiza o contrato HTTP. Internamente, o modelo legado ainda armazena Base64
no banco; o relatório não afirma que essa camada de armazenamento foi modernizada
para object storage.

### 21.4 Por que existe um serviço de download e uma chamada ao DOM


**Código real da implementação:** `Datagram WEB/Datagram-frontend/src/app/shared/file.service.ts`

```typescript
download(blob: Blob, name: string) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = name;
    a.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
}
```

`URL.createObjectURL(blob)` cria uma referência temporária aos bytes locais.
O link com atributo `download` inicia o download. `URL.revokeObjectURL(url)` libera
a referência depois do uso, em vez de acumulá-la durante a vida da página.

O timer de um segundo é uma decisão prática para não revogar a URL antes de o
navegador iniciar o download. Não é uma confirmação de término da transferência.

A chamada `document.createElement('a')` foi mantida nessa fronteira específica de
integração com o navegador. A regra não é "nunca usar DOM"; é evitar controlar o
estado visual de componentes inteiros por IDs, quando o Angular já oferece bindings.
O serviço encapsula essa necessidade localizada e evita copiar a mesma lógica em
três telas.

## 22. DTO e `record` no Java: definir contratos em vez de expor entidades

### 22.1 Entrada de edição limitada


**Código real da implementação:** `Datagram WEB/Datagram-backend/src/main/java/com/datagram/datagramweb/api/ApiDtos.java`

```java
public record Profile(
      @NotBlank @Size(max = 120) String nome,
      @Size(max = 120) String escolaridade,
      @Size(max = 2) String estado,
      @Size(max = 120) String municipio,
      @Size(max = 200) String instituicao,
      @Size(max = 1000) String linkCv,
      @Size(max = 30) Set<@Size(max = 80) String> interesses) {}
```

`Profile` é um DTO de comando: descreve os dados que a edição de perfil pode receber.
Ele não contém ID de destino, tipo de usuário, senha, quantidade de seguidores ou
quem está seguindo quem. A ausência desses campos é uma decisão de domínio e
permissão, não uma limitação do formulário.

No código anterior, aceitar uma entidade inteira acoplava o request ao modelo de
persistência e permitia que campos sem relação com a edição chegassem ao serviço.
No novo fluxo, o serviço escolhe o usuário autenticado e copia apenas os campos
explicitamente editáveis.

### 22.2 O que `record` gera e o que não gera

Um record é uma classe de dados de Java, com componentes declarados no cabeçalho.
O compilador gera construtor canônico, accessors como `nome()`, `equals`, `hashCode`
e `toString`, e os campos de componentes são finais.

```java
// Exemplo didático equivalente ao estilo usado no projeto.
public record Login(String email, String senha) {}

Login request = new Login("test@example.com", "exemplo");
String email = request.email(); // não é getEmail().
```

Isso reduz boilerplate em contratos que não precisam de ciclo de vida de entidade.
Não significa imutabilidade profunda: se um record carrega uma coleção mutável,
essa coleção não fica automaticamente congelada. Por isso `UserView.of` usa
`Set.copyOf` ao montar os interesses de saída.

O `toString` gerado pode incluir campos sensíveis. Usar record para login/cadastro
não autoriza registrar seu conteúdo em logs; a aplicação não deve logar senhas.

### 22.3 Por que a entidade JPA não virou record

Entidades precisam de identidade persistente, controle de alterações e um ciclo de
vida gerenciado pelo provedor JPA. Os DTOs, por outro lado, são contratos de entrada
e saída. O projeto manteve entidades como classes e usou records na fronteira HTTP.

Isso evita confundir "usuário no banco" com "usuário que esta pessoa pode enxergar".
A resposta pública omite senha e CPF e só inclui email quando apropriado ao próprio
usuário. O fato de o banco possuir um campo não implica que ele deva aparecer no JSON.

## 23. Bean Validation e anotações de controller

### 23.1 O papel de cada anotação de validação

| Anotação | O que verifica | Aplicação concreta |
| --- | --- | --- |
| `@NotBlank` | Texto não nulo e com algum conteúdo não branco | Nome, título e credenciais obrigatórios |
| `@Email` | Formato compatível com a validação de email adotada pela biblioteca | Entrada de email; não comprova que a caixa existe |
| `@Size(max=...)` | Tamanho de string/coleção | Limite de nome, texto e número de interesses |
| `Set<@Size(max=80) String>` | Restrição nos elementos da coleção | Limite por interesse, além do limite da coleção |
| `@Valid` | Aciona validação do objeto recebido nessa fronteira | Request DTO no controller |

Colocar `@Size` num record e esquecer de acionar a validação na entrada não é o
mesmo que ter um contrato HTTP efetivamente validado. As regras de negócio também
não se esgotam nessas anotações: maioridade, autorização e URL permitida exigem
lógica adicional.

### 23.2 A edição real de perfil

```java
// Trecho real do UsuarioController.
@PutMapping({"", "/"})
public UserView update(@Valid @RequestBody Profile data) {
  return UserView.of(users.updateProfile(data), true);
}
```

`@RequestBody` pede ao Spring/Jackson que converta o JSON no tipo declarado.
`@Valid` aplica as constraints desse DTO. `@PutMapping` mapeia a atualização; as
alternativas vazia e barra tratam os dois caminhos aceitos pelo controller base.

O retorno agora é `UserView`, porque o frontend precisa do estado confirmado.
Isso não torna 204 errado em geral: 204 é correto quando não há conteúdo e o cliente
foi projetado para isso. O defeito anterior era o desacordo entre os lados.

### 23.3 Outros parâmetros e retornos

- `@RestController`: combina o papel de controller com serialização do retorno
  como resposta HTTP; não é um template HTML do servidor.
- `@RequestMapping("/user")`: define o prefixo dos métodos daquela classe.
- `@PathVariable Integer id`: lê um segmento como `/post/42`.
- `@RequestParam`: lê parâmetro de consulta ou campo multipart, conforme o caso.
- `@RequestPart`: usado no cadastro de post com PDF para separar JSON e arquivo.
- `ResponseEntity<T>`: permite controlar status, headers e corpo explicitamente.
- `@ResponseStatus(NO_CONTENT)`: usado na exclusão que termina sem corpo.

**Semântica HTTP adotada:** 201 para criação com Location, 200 quando há conteúdo de
resposta, 204 para exclusão sem conteúdo, 400 para entradas inválidas, 401 para
falta de autenticação válida, 403 para operação proibida, 404 para recurso ausente,
409 para conflitos e 413 para limite multipart excedido.

O status não substitui um contrato claro. O frontend trata mensagem JSON quando
existe e oferece fallback quando a infraestrutura de segurança retorna apenas status.

### 23.4 Por que centralizar erros


**Código real da implementação:** `Datagram WEB/Datagram-backend/src/main/java/com/datagram/datagramweb/api/ApiErrors.java`

```java
@RestControllerAdvice
public class ApiErrors {
  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<Map<String, String>> status(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode())
        .body(Map.of("message", e.getReason() == null ? "Operação inválida" : e.getReason()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Map<String, String>> invalid(MethodArgumentNotValidException e) {
    var error = e.getBindingResult().getFieldErrors().get(0);
    return ResponseEntity.badRequest()
        .body(Map.of("message", error.getField() + ": " + error.getDefaultMessage()));
  }
```

O recorte omite os demais handlers. `@RestControllerAdvice` define tratamento
transversal para os controllers; `@ExceptionHandler` associa cada método a um tipo
de exceção. Isso evita repetir try/catch de infraestrutura em toda operação.

O código retorna uma mensagem pública e status consistente. Não envia stack trace
ou senha ao navegador. Ainda há limites: o handler de validação apresenta o primeiro
erro de campo, não uma coleção completa de erros por controle; erros de segurança
podem sair antes do controller, pela filter chain.

## 24. Spring: injeção, `@Service`, `@Configuration` e `@Bean`

### 24.1 Injeção por construtor no serviço


**Código real da implementação:** `Datagram WEB/Datagram-backend/src/main/java/com/datagram/datagramweb/Service/PostagemService.java`

```java
@Service
@Transactional(readOnly = true)
public class PostagemService {
  private final PostagemRepository repo;
  private final UsuarioRepository users;
  private final UsuarioService session;
  private final NotificacaoRepository notifications;

  public PostagemService(
      PostagemRepository p, UsuarioRepository u, UsuarioService s, NotificacaoRepository n) {
    repo = p;
    users = u;
    session = s;
    notifications = n;
  }
```

`@Service` registra a classe como componente de serviço detectável pelo Spring e
comunica sua responsabilidade de aplicação/domínio. Não confere automaticamente
segurança, transação ou correção a todos os métodos.

Os campos `private final` declaram dependências que não serão trocadas depois da
construção. O construtor recebe essas dependências do contêiner. Com um único
construtor, não é necessário adicionar `@Autowired` nele nas versões de Spring
usadas aqui. Existe um uso explícito no controller, mas ele não é a razão para o
padrão de injeção funcionar.

O serviço consegue ser testado com dependências controladas sem montar manualmente
um HttpClient/EntityManager dentro de cada método. O princípio é dependência
explícita, e não a obrigação de escolher entre sintaxe de campo e de construtor
por estética.

### 24.2 Bean de PasswordEncoder

```java
// Trecho real de SecurityConfig.
@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
  // filterChain omitida neste recorte
}
```

`@Configuration` identifica uma classe de configuração do contêiner. `@Bean`
registra o objeto retornado pelo método. Assim os serviços recebem a mesma política
de senha configurada pelo Spring, em vez de cada um criar um encoder com regras
potencialmente diferentes.

`PasswordEncoder` é a abstração injetada; BCrypt é a implementação escolhida.
Trocar a política exige planejar compatibilidade/migração dos hashes existentes;
não basta trocar uma classe sem considerar os dados já gravados.

## 25. JWT, BCrypt e autorização por recurso

### 25.1 Autenticação e autorização não são a mesma pergunta

Autenticação identifica quem apresentou credencial válida. Autorização decide se
essa identidade pode executar uma ação sobre um recurso. Um usuário autenticado
não ganha por isso permissão para editar a postagem de outra pessoa.

### 25.2 O que mudou no login

```java
// Trecho real, extraído de UsuarioService.login().
Usuario user = repo.findByEmail(email.trim().toLowerCase(Locale.ROOT));
if (user == null
    || password == null
    || user.getSenha() == null
    || !passwords.matches(password, user.getSenha())) {
  throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
}
return Map.of("token", jwt.generateToken(user));
```

Email é normalizado com locale explícito. Senha **não** é convertida em minúsculas:
a comparação agora é sensível à capitalização. O teste verifica que uma senha com
letras de caixa diferente não autentica.

BCrypt guarda uma representação com salt e custo computacional. O login usa
`matches` para verificar o valor informado contra o hash persistido. Não descriptografa
a senha nem compara novamente dois hashes gerados com salts diferentes.

O limite de 72 indicado no DTO é em caracteres para a validação `@Size`; BCrypt
tem particularidades de limite em bytes de entrada. Uma revisão específica para
senhas Unicode/extensas e sua política continua sendo um refinamento possível.
Não se deve interpretar esse DTO como demonstração de que todo caso de fronteira
criptográfico já foi coberto pelos testes.

### 25.3 O papel do JWT e do filtro

Após login, o backend assina um token. No acesso seguinte, o filtro lê Authorization,
valida assinatura/expiração e resolve o usuário correspondente. O nome do principal
passou a ser o email esperado pelo serviço de sessão.

O token não é uma senha cifrada nem uma obrigação de armazenar dados pessoais nele.
Assinar o JWT permite detectar alteração do seu conteúdo; não torna todo payload
secreto. O segredo de assinatura fica fora do Git em produção.

Nesta implementação, o servidor reconsulta o usuário ao processar a identidade.
Não há refresh token nem lista de revogação antecipada: logout limpa a aba, mas um
JWT copiado ainda pode ser válido até expirar. Esse limite está documentado.

### 25.4 Restrição por papel e por autoria

```java
// Trecho real da configuração de autorização.
.requestMatchers("/gerente/**").hasRole("GERENTE")
.anyRequest().authenticated()
```

O filtro constrói uma autoridade com prefixo `ROLE_`. `hasRole("GERENTE")` usa a
convenção de papel do Spring. Isso protege o conjunto de endpoints de gerente.

Para recurso individual, a checagem de papel não basta:


**Código real da implementação:** `Datagram WEB/Datagram-backend/src/main/java/com/datagram/datagramweb/Service/PostagemService.java`

```java
public Postagem own(Integer id) {
    var p = find(id);
    if (!p.getAutor().getId().equals(session.authenticated().getId()))
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Somente o autor pode alterar esta postagem");
    return p;
  }
```

`equals` compara o valor do ID. O padrão antigo com `Integer == Integer` podia
comparar referências de objetos e apresentar comportamento diferente conforme os
valores/instâncias. Consultas específicas ao banco também removeram a necessidade
de filtrar listas inteiras com essa comparação.

`own(id)` é chamado pelos métodos que alteram/excluem postagens. Assim, esconder
um botão no Angular não é a defesa que protege os dados.

### 25.5 Stateless, CSRF e CORS não são uma mesma opção

- `STATELESS` configura a política de sessão HTTP no Spring Security. Não significa
  que a aplicação inteira deixou de ter estado: há banco e há estado no navegador.
- O JWT via Authorization não depende, neste desenho, de cookie de sessão enviado
  automaticamente pelo browser. É nesse contexto que a chain desabilita CSRF.
  Se a autenticação migrar para cookies, a decisão precisa ser reavaliada.
- CORS limita origens de requisições de navegador conforme a configuração. Não
  autentica usuários e não bloqueia, sozinho, um cliente HTTP fora do navegador.
- `@Value` lê a configuração de origem/segredo. A anotação não guarda o segredo com
  segurança por si só; a fonte da configuração e o ambiente de deploy importam.

## 26. `@Transactional`, idempotência e `@Version`

### 26.1 Uma ação de usuário pode alterar várias linhas


**Código real da implementação:** `Datagram WEB/Datagram-backend/src/main/java/com/datagram/datagramweb/Service/PostagemService.java`

```java
@Transactional
  public Postagem insert(PostInput d, String attachment) {
    Usuario me = session.authenticated();
    Postagem p = new Postagem();
    apply(p, d);
    p.setAutor(me);
    p.setDate(java.time.Instant.now().toString());
    p.setArquivoPublicacao(attachment);
    p = repo.save(p);
    me.setPosts(1);
    users.save(me);
    for (Integer follower : me.getIdsSeguidores())
      notifications.save(new Notificacao(session.find(follower), p.getId()));
    return p;
  }
```

Criar post também define autor e data, incrementa o contador e cria notificações.
A intenção é que essas alterações no banco participem da mesma transação.
`@Transactional` estabelece a fronteira gerenciada pelo Spring.

Se uma exceção de runtime causar rollback nessa fronteira, não deve ficar somente
metade dessas alterações confirmada. Isso não abrange efeitos externos como um
email já enviado ou um arquivo gravado em outro serviço: eles exigiriam outra
estratégia, como outbox/compensação, se fossem adicionados.

`me.setPosts(1)` merece uma observação: o método legado da entidade **incrementa**
a quantidade; ele não substitui o total por 1. O serviço usa o comportamento
existente, mas renomeá-lo para `incrementPosts` tornaria essa intenção mais evidente.
A documentação não apresenta esse nome herdado como um padrão ideal de API.

### 26.2 Read-only e detalhe de proxy

A classe de serviço tem `@Transactional(readOnly = true)` para leituras e os métodos
de escrita declaram `@Transactional` próprio. `readOnly` comunica uma intenção e
pode permitir otimizações do provedor; não deve ser tratado como mecanismo de
permissão que torna qualquer escrita impossível.

Na configuração usual de Spring baseada em proxy, o comportamento transacional é
aplicado quando a chamada atravessa o proxy. Uma chamada interna `this.metodo()`
não cria automaticamente uma nova interceptação. Além disso, os controllers desta
branch também possuem fronteira transacional, de modo que uma chamada do serviço
normalmente participa da transação já aberta. Isso mantém o mapeamento de coleções
JPA dentro da fronteira, com Open Session in View desligado.

Portanto, não é exato dizer que cada anotação abre sempre uma transação nova ou
que a dica read-only prevalecerá sobre uma transação de escrita externa. Refinar a
fronteira para uma facade de aplicação que já retorne DTO seria uma organização
adicional possível, se o tamanho do domínio justificar.

### 26.3 Por que curtir deixou de ser "mande um contador maior"


**Código real da implementação:** `Datagram WEB/Datagram-backend/src/main/java/com/datagram/datagramweb/Service/PostagemService.java`

```java
@Transactional
  public Postagem like(Integer id, boolean like) {
    Postagem p = find(id);
    Integer me = session.authenticated().getId();
    Set<Integer> likes = new HashSet<>(p.getIdsCurtida());
    if (like) likes.add(me);
    else likes.remove(me);
    p.setAllIdsCurtida(likes);
    return repo.save(p);
  }
```

O servidor obtém o ID autenticado e opera sobre um conjunto. Um `Set` não contém o
mesmo ID duas vezes. Repetir PUT de like acrescenta a mesma identidade, sem duplicar;
DELETE a remove. O total é derivado das identidades registradas.

É diferente de um endpoint "toggle": repetir toggle pode desfazer o efeito da
primeira chamada. Com PUT/DELETE de intenção explícita, uma repetição da mesma
operação tem resultado de relação estável. Isso é idempotência observável sobre
aquele estado, não uma promessa de que nenhum processamento/log será repetido.

### 26.4 O que `@Version` detecta

```java
// Trecho real das entidades, com imports e outros campos omitidos.
@Version
@JsonIgnore
private Long version;
```

Exemplo didático: duas transações carregam a versão 7 de um post. Uma grava e a
versão avança. Quando a outra tenta gravar com a versão anterior, o provedor pode
detectar a atualização concorrente em vez de sobrescrever silenciosamente o estado.
A API transforma o conflito otimista em 409.

`@Version` é uma anotação de controle de concorrência JPA. `@JsonIgnore` impede que
esse campo apareça na serialização usual da entidade; os DTOs também não o expõem.
O campo não é um lock pessimista mantido durante toda edição no navegador, e não
resolve por si só todos os conflitos de formulários salvos muito tempo depois.

Como o DTO de edição não carrega a versão que o navegador originalmente leu,
esta versão protege principalmente concorrência entre transações do servidor que
trabalham sobre a mesma versão persistente. Um protocolo explícito de edição
condicional, com versão/ETag do cliente e If-Match, seria necessário para detectar
todo rascunho de cliente desatualizado. Isso não foi implementado nesta branch.

## 27. JPA, consultas específicas e paginação

### 27.1 Repository e consulta derivada


**Código real da implementação:** `Datagram WEB/Datagram-backend/src/main/java/com/datagram/datagramweb/Repository/PostagemRepository.java`

```java
public interface PostagemRepository extends JpaRepository<Postagem, Integer> {
  List<Postagem> findTop50ByAutorIdOrderByIdDesc(Integer id);

  List<Postagem> findTop10ByAutorIdOrderByCurtidaDescIdDesc(Integer id);

  Slice<Postagem> findByAutorIdIn(Collection<Integer> ids, Pageable pageable);

  Slice<Postagem> findByAutorId(Integer id, Pageable pageable);
```

`JpaRepository<Postagem, Integer>` declara a entidade e o tipo de ID. A infraestrutura
gera a implementação do repositório; não é necessário escrever uma classe manual
para cada método básico de persistência.

Nos métodos derivados, o nome faz parte do contrato interpretado pelo Spring Data:

- `findTop50ByAutorIdOrderByIdDesc`: restringe por ID do autor, limita a 50 e ordena.
- `findTop10ByAutorIdOrderByCurtidaDescIdDesc`: ranking de curtidas com desempate por ID.
- `findByAutorIdIn`: filtra por uma coleção de autores, usada no feed.
- `Pageable`: carrega a especificação de página, limite e ordenação.

Não é o nome comprido em si que torna a consulta eficiente; são o filtro e o limite
aplicados no banco e os índices/plano de execução adequados. A branch não apresenta
EXPLAIN nem teste de carga que prove a melhor indexação para um banco de produção.

### 27.2 Por que `Slice` em vez de buscar tudo ou exigir total

```java
// Trecho real do serviço de paginação, com validação e outros ramos omitidos.
var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
return repo.findByAutorIdIn(ids, pageable);
```

A interface precisa de itens e da informação "há mais?". `Slice` fornece esse modelo
sem exigir o total global que um `Page` normalmente precisa calcular. O contrato
HTTP devolve `{content, hasNext}`.

No Angular, a lista recebe uma nova referência ao acrescentar resultados:

```typescript
// Trecho real de ProfileComponent.more().
this.posts.update((p) => [
  ...p,
  ...r.content.filter((n) => !p.some((x) => x.id === n.id)),
]);
```

O spread cria outro array, preservando os anteriores, e o filtro evita IDs já
presentes. Isso ajuda a consistência visual, mas não transforma paginação por
offset em paginação por cursor. Se houver exclusões/inserções entre duas páginas,
a posição dos itens pode mudar e algum item pode ser omitido nessa travessia.

### 27.3 Por que não ordenar a data de apresentação como string

Textos como `31/01/2026` e `02/02/2026` não têm a mesma ordenação lexicográfica e
cronológica. Os novos posts recebem `Instant` no servidor e usam ordem por ID
decrescente no feed. Isso evita usar a aparência de uma data local como critério
principal de ordenação.

O frontend ainda permite mostrar texto de data legado quando não consegue
interpretá-lo como Date. Esse fallback não converte automaticamente todo histórico
para um instante exato. Registros antigos e banco real precisam da migração
específica descrita no roteiro.

### 27.4 Anotações de entidade que aparecem na refatoração

| Construção | Responsabilidade | Motivo no projeto |
| --- | --- | --- |
| `@Entity` | Declara entidade persistente gerenciada por JPA | Mantém o modelo de dados no backend |
| `@Id` | Identifica a chave da entidade | Distingue recursos persistentes |
| `@GeneratedValue(strategy = IDENTITY)` | Delega geração de ID ao mecanismo de identidade do banco | Evita depender do gerador legado de incremento no processo |
| `@Column(length=...)` / `columnDefinition="text"` | Define aspectos do mapeamento de coluna | Compatibiliza armazenamento com conteúdo e URLs aceitos |
| `@Version` | Controla versão persistente para concorrência otimista | Detecta certas gravações concorrentes |
| `@JsonProperty(access = WRITE_ONLY)` | Controla acesso JSON de um campo | Reforça que senha não deve sair numa serialização da entidade |
| `@JsonIgnore` | Omite propriedade na serialização usual | Não expõe coleções internas/versão inadvertidamente |

Mapeamento de coluna não substitui validação HTTP. `@JsonIgnore` não é uma regra de
autorização. Geração de identidade não significa que o projeto foi testado com
vários bancos ou múltiplas instâncias. E mudar essas anotações exige migração do
esquema existente; não basta compilar contra um H2 novo.

### 27.5 Um cuidado que permaneceu

Algumas relações ainda usam carregamento eager e os DTOs acessam associações JPA.
A refatoração reduziu payload e scans gerais, mas não substituiu tudo por projeções
SQL nem eliminou todo potencial N+1. Otimizar isso corretamente exige medir as
consultas e o volume real; trocar todas as relações para lazy indiscriminadamente
poderia apenas criar erros de inicialização fora de transação.

## 28. Pequenas construções de linguagem que aparecem nos exemplos

| Sintaxe/API | Como ler | Observação importante |
| --- | --- | --- |
| `() => valor` | Função que calcula/devolve valor | No computed, as leituras reativas dentro dela definem dependências |
| `valor => {...}` | Callback com um parâmetro | Não executa sozinho até quem o recebeu invocá-lo |
| `{ tags, ...d }` | Desestrutura e separa uma propriedade do restante | Não faz validação nem cópia profunda |
| `{ ...u, nome: novo }` | Cria objeto raso com nome substituído | Campos aninhados continuam compartilhados se não forem copiados |
| `lista.map(...)` | Cria lista transformada | Diferente de mutar item por item em um estado compartilhado |
| `lista.filter(...)` | Cria lista só com itens aprovados | Usado para remover um post da lista local |
| `u?.nome` | Acesso opcional | Pode produzir undefined; não confirma que o objeto existe |
| `valor!` | Asserção não nula no sistema de tipos | Não insere proteção na execução |
| `void router.navigateByUrl(...)` | Descarta intencionalmente o valor da Promise | Não captura, por si só, uma rejeição dessa Promise |
| `as HTMLInputElement` | Asserção de tipo | Baseia-se no elemento esperado pelo binding; não é um cast verificado pelo browser |
| `instanceof HttpErrorResponse` | Verificação runtime e refinamento do tipo | Permite acessar propriedades HTTP a partir de unknown |
| `import('./pagina')` | Importação dinâmica | Produz Promise e permite separação de bundle |
| Java `var` | Inferência local estática | Não transforma Java numa linguagem dinamicamente tipada |
| Java `Optional.orElseThrow(...)` | Resolve presença ou lança exceção definida | Usado para transformar recurso ausente em 404 |
| Java `stream().map(...).toList()` | Projeta uma coleção para outra representação | Não é o mesmo stream temporal de RxJS |

### 28.1 `@if`, `@for` e `track`

```html
<!-- Trecho real do feed. -->
@for (post of posts(); track post.id) {
  <app-post-card
    [post]="post"
    (updated)="update($event)"
    (deleted)="remove($event)"
  />
}
```

`@for` é controle de fluxo do template, não decorator TypeScript nem anotação Java.
`track post.id` fornece uma identidade estável para reconciliar itens da lista.
Assim o Angular pode associar um post à mesma instância visual quando sua posição
ou referência muda, evitando reconstruções desnecessárias.

Não é correto prometer que `track` torna qualquer lista barata: criar milhares de
cards e imagens continua tendo custo. Por isso a paginação e a identificação estável
foram usadas juntas. `@if` expressa renderização condicional; é diferente de manter
um elemento presente e só trocar seu display por manipulação direta de DOM.

## 29. Como os testes apoiam as decisões de arquitetura

### 29.1 Teste Angular de estado compartilhado e confirmação do servidor


**Código real da implementação:** `Datagram WEB/Datagram-frontend/src/app/pages/photo.spec.ts`

```typescript
it('changes the image in both profile and persistent header after the server confirms', () => {
    const f = profile();
    const header = TestBed.createComponent(LayoutComponent);
    header.detectChanges();
    f.componentInstance.photo(
      event(new File(['png'], 'photo.png', { type: 'image/png' })),
    );
    const r = http.expectOne('/api/user/foto');
    expect(r.request.body instanceof FormData).toBe(true);
    expect(session.user()?.fotoPerfil).toContain('old');
    r.flush({ ...sampleUser, fotoPerfil: '/api/user/1/foto?v=new' });
    f.detectChanges();
    header.detectChanges();
    expect(
      f.nativeElement.querySelector('app-avatar img').getAttribute('src'),
    ).toContain('new');
    expect(
      header.nativeElement.querySelector('app-avatar img').getAttribute('src'),
    ).toContain('new');
  });
```

O teste cria perfil e cabeçalho como consumidores distintos. Antes de responder ao
HTTP, verifica que a sessão ainda contém a imagem antiga. Depois de simular o
retorno confirmado, verifica os dois elementos de imagem.

`HttpTestingController` não executa um backend real; ele permite controlar a ordem
e o resultado das requisições do frontend. Isso é justamente o que permite testar
falha, demora e cancelamento de forma determinística. A integração real é coberta
separadamente pelos testes Java e de navegador.

Há testes complementares para erro preservando a foto, cancelamento de seleção,
retry do mesmo fluxo, validade de formulários sem Zone e mudança de parâmetro
cancelando a consulta anterior no mesmo componente.

### 29.2 Teste Java de permissão e identidade

No `ApiIntegrationTests`, um usuário cria o post e outro tenta editar/excluir.
A suíte espera 403. Isso comprova um comportamento do servidor que não pode ser
provado apenas pela ausência do botão no template.

MockMvc exercita a infraestrutura Spring e a cadeia de segurança dentro do teste,
com banco H2 e dados artificiais. Não equivale a testar o proxy da hospedagem,
o banco real do usuário ou o navegador.

O teste de like envia a operação duas vezes e espera total 1, autor preservado e
`curtido=true` para o usuário autenticado. É uma verificação da invariante que
motivou separar o comando de like do comando de edição.

### 29.3 Teste de navegador: a prova específica sobre F5

```typescript
// Trechos reais do cenário Playwright, não um código do produto.
await page.evaluate(() => {
  (window as unknown as { spaMarker: string }).spaMarker = 'persistent';
});

// O cenário executa navegação, foto, edição e publicação entre esses pontos.

expect(
  await page.evaluate(
    () => (window as unknown as { spaMarker: string }).spaMarker,
  ),
).toBe('persistent');
```

Uma recarga completa substituiria o documento e perderia essa propriedade criada
em window. O teste verifica que ela continua presente depois dos fluxos. É evidência
mais direta sobre a ausência de reload do que simplesmente buscar a string
`location.reload` no repositório.

Isso não demonstra que todo link possível, toda falha de rede e toda configuração
de servidor foram cobertos. O teste percorre os fluxos listados; o relatório evita
estender a garantia para situações que não foram executadas.

### 29.4 Por que não reexecutar todas as suítes ao editar somente este relatório

A atualização atual é de documentação. Os resultados funcionais continuam sendo
os da refatoração validada: 12 testes Java, 12 Angular e três cenários de navegador.
Nesta edição, a verificação relevante é outra: origem dos trechos, coerência das
explicações, links internos e integridade do Markdown. Não há ganho em anunciar
novas validações funcionais quando nenhum comportamento foi alterado.

## 30. Mapa de responsabilidades depois da refatoração

| Arquivo/classe | Responsabilidade principal | O que não deve passar a concentrar |
| --- | --- | --- |
| `SessionService` | Usuário autenticado e ciclo local de sessão | Regras de autorização do banco |
| `ApiService` | Endpoints, corpos e tipos HTTP | Modal, navegação e apresentação de cada tela |
| `authInterceptor` | Token na API e falha de autenticação | Validação de conteúdo de formulário |
| `app.routes.ts` / layout | Composição de páginas e navegação | Cópias independentes da sessão em cada rota |
| `ProfileComponent` | Coordenar leitura/ações do perfil apresentado | Conversão de PDF copiada de outras páginas |
| `ProfileFormComponent` | Campos, validação e envio da edição | Determinar que pode editar outra pessoa |
| `AvatarComponent` | Apresentar foto/fallback | Buscar senha, realizar login ou decidir permissões |
| `PostEditorComponent` | Modelo e submissão de postagem | Controlar diretamente a lista interna do feed pai |
| `PostCardComponent` | Apresentar post e emitir resultados de ações | Aceitar contadores do browser como verdade do servidor |
| `FileService` | Validação básica local e download | Substituir a validação de arquivo no backend |
| Controllers | Adaptar HTTP e DTOs | Confiar em autoria enviada pelo cliente |
| Services Java | Regras, identidade e operações transacionais | Expor livremente o grafo de entidades no JSON |
| Repositories | Consultar/persistir com filtro e limite | Decidir apresentação de botões no Angular |
| DTOs | Contratos por intenção e visão pública | Reproduzir toda entidade por conveniência |

Esse mapa é uma orientação de manutenção. Algumas decisões continuam pragmáticas:
a foto é persistida pelo controller com o repositório injetado e o mapeamento de
DTO usa fronteira transacional também no controller. Mover isso para uma facade
seria um próximo passo de organização, não uma funcionalidade já existente que
esta documentação pretende simular.

## 31. Por que essa remodelagem, e até onde ela vai

O principal ganho não é trocar uma tecnologia antiga por um nome moderno. É tornar
rastreável a sequência: **intenção da pessoa, comando, validação/autorização,
persistência, resposta e estado que a tela apresenta**.

Um exemplo completo é a foto: o componente visual não salva por conta própria;
a tela coordena a seleção, o cliente HTTP transporta, o servidor decide o usuário,
a resposta confirma a mudança e a sessão compartilha o resultado. Cada fronteira
tem um contrato que pode ser observado num teste.

Outro é a curtida: a intenção não é deduzida de um número adulterável. Ela é uma
operação específica sobre a relação entre usuário autenticado e post existente.
O resultado contém o estado necessário para a interface e não altera o autor.

Os padrões aplicados são coesão, separação de responsabilidades, fluxo de dados
explícito, UI declarativa, injeção de dependências, contratos DTO, autorização por
recurso, transações, controle otimista de concorrência, idempotência e consultas
limitadas. Eles ajudam a manter invariantes claras; não são uma certificação de
que qualquer sistema que os use está automaticamente correto.

### Decisões e alternativas

| Escolha feita | Por que atendia ao Datagram | Alternativa que não foi imposta |
| --- | --- | --- |
| Serviço de sessão com Signals | Estado compartilhado pequeno e atualização direta de consumidores | NgRx para um domínio com orquestração mais complexa |
| RxJS para HTTP/rota/busca | Composição temporal e cancelamento já integrados ao Angular | Misturar vários callbacks independentes |
| Reactive Forms tipados | Regras e controles explícitos, com testes | Reescrever tudo em outra API de formulários sem necessidade |
| Standalone e lazy routes | Dependências locais e carregamento inicial menor | NgModules bem separados também seriam viáveis |
| Resposta de edição com UserView | Atualiza a UI a partir do resultado confirmado | 204 seguido de leitura explícita também seria um contrato válido |
| Atualização confirmada | Preserva estado anterior em caso de erro | Otimista exigiria rollback/reconciliação adicional |
| Slice com offset | Interface precisa de itens e hasNext | Cursor seria mais robusto sob alterações simultâneas |
| Arquivo separado no HTTP | Evita anexos repetidos em listagens | Object storage exige migração própria de infraestrutura/dados |
| JWT em Authorization | Alinha o fluxo existente do backend e interceptor | Cookie HttpOnly exigiria reavaliar CSRF e o contrato de sessão |

### O que não se deve concluir do relatório

Não há atualização automática entre dispositivos, não houve migração do banco
real, não há teste de carga ou prova de ausência de N+1, não há garantia absoluta
contra XSS por usar Angular, e não se adaptou o Android aos novos contratos.
A correção demonstrada é a dos fluxos e invariantes especificados, com as evidências
registradas e as limitações expostas.

## 32. Referências de consulta para estudar os exemplos

Os trechos de código vêm do Datagram e as justificativas foram escritas para seus
fluxos. As referências abaixo servem para conferir o comportamento das APIs, sem
substituir a leitura dos exemplos e os testes do projeto.

| Tema | Referência primária |
| --- | --- |
| Signals e valores derivados | https://angular.dev/guide/signals |
| Entrada de componentes | https://angular.dev/guide/components/inputs |
| Eventos de saída | https://angular.dev/guide/components/outputs |
| Integração RxJS/Signals | https://angular.dev/ecosystem/rxjs-interop |
| Ciclo de vida da assinatura | https://angular.dev/ecosystem/rxjs-interop/take-until-destroyed |
| Execução sem Zone.js | https://angular.dev/guide/zoneless |
| Formulários reativos | https://angular.dev/guide/forms/reactive-forms |
| Requisições HTTP | https://angular.dev/guide/http/making-requests |
| Guards e limite de segurança | https://angular.dev/guide/routing/route-guards |
| TypeScript: tipos e uniões | https://www.typescriptlang.org/docs/handbook/2/everyday-types.html |
| Records no Java 21 | https://docs.oracle.com/en/java/javase/21/language/records.html |
| Transações declarativas | https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html |
| Consultas Spring Data JPA | https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html |
| PasswordEncoder | https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/password-encoder.html |

As referências Spring atuais podem apresentar versões mais novas. O projeto desta
branch continua em Spring Boot 3.2.9/Java 21; não se afirma que adotou todos os
recursos mostrados nas versões mais novas da documentação.
