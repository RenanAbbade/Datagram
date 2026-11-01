package com.datagram.datagramweb;

import com.fasterxml.jackson.databind.*;
import com.datagram.datagramweb.Repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class ApiIntegrationTests {
 @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired UsuarioRepository users;
 record Account(String token,int id,String email) {}
 Map<String,Object> registration(String email){return new HashMap<>(Map.of("nome","Pesquisador teste","email",email,"senha","Password123!","dataNasc","1990-02-15","tipoUsuario","Pesquisador","dataInicio","2020-01-01","instituicao","Universidade"));}
 Account account() throws Exception {
  String email=UUID.randomUUID()+"@example.com";
  mvc.perform(post("/user/insert").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(registration(email)))).andExpect(status().isCreated()).andExpect(jsonPath("$.senha").doesNotExist());
  var result=mvc.perform(post("/user/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("email",email,"senha","Password123!")))).andExpect(status().isOk()).andReturn();
  var response=json.readTree(result.getResponse().getContentAsString());return new Account(response.get("token").asText(),response.get("usuario").get("id").asInt(),email);
 }
 String bearer(Account a){return "Bearer "+a.token;}
 String postBody(String title){return "{\"titulo\":\""+title+"\",\"conteudo\":\"Conteúdo\",\"tipoPostagem\":\"simples\",\"palavrasChave\":[]}";}
 int create(Account a) throws Exception {return json.readTree(mvc.perform(post("/post/insert").header("Authorization",bearer(a)).contentType(MediaType.APPLICATION_JSON).content(postBody("Publicação"))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asInt();}
 @Test void authenticationIsRealAndPasswordsAreNotExposed() throws Exception {
  var a=account();assertThat(users.findByEmail(a.email).getSenha()).startsWith("$2");
  mvc.perform(get("/user/userLogado").header("Authorization",bearer(a))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(a.id)).andExpect(jsonPath("$.senha").doesNotExist()).andExpect(jsonPath("$.cpf").doesNotExist());
  mvc.perform(get("/user/userLogado")).andExpect(status().isUnauthorized());
  mvc.perform(get("/user/userLogado").header("Authorization","Bearer invalid")).andExpect(status().isUnauthorized());
  mvc.perform(post("/user/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("email",a.email,"senha","password123!")))).andExpect(status().isUnauthorized());
 }
 @Test void registrationRejectsPrivilegesAndUnderageDates() throws Exception {
  var d=registration(UUID.randomUUID()+"@example.com");d.put("tipoUsuario","Gerente");
  mvc.perform(post("/user/insert").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(d))).andExpect(status().isBadRequest());
  d.put("tipoUsuario","Pesquisador");d.put("dataNasc",java.time.LocalDate.now().minusYears(17).toString());
  mvc.perform(post("/user/insert").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(d))).andExpect(status().isBadRequest());
 }
 @Test void profileCannotOverwriteAnotherUserOrCounters() throws Exception {
  var a=account();var other=account();
  String body="{\"id\":"+other.id+",\"nome\":\"Novo nome\",\"tipoUsuario\":\"Gerente\",\"seguidores\":999,\"interesses\":[\"Java\"]}";
  mvc.perform(put("/user").header("Authorization",bearer(a)).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(a.id)).andExpect(jsonPath("$.tipoUsuario").value("Pesquisador")).andExpect(jsonPath("$.seguidores").value(0));
  assertThat(users.findById(other.id).orElseThrow().getNome()).isEqualTo("Pesquisador teste");
  mvc.perform(get("/gerente/mediaSeguidores").header("Authorization",bearer(a))).andExpect(status().isForbidden());
 }
 @Test void followIsIdempotentAndNotifiesFollowers() throws Exception {
  var a=account();var b=account();
  for(int i=0;i<2;i++)mvc.perform(put("/user/"+b.id+"/follow").header("Authorization",bearer(a))).andExpect(status().isOk()).andExpect(jsonPath("$.seguidores").value(1));
  create(b);
  mvc.perform(get("/post/feed").header("Authorization",bearer(a))).andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1));
  mvc.perform(get("/notificacao/").header("Authorization",bearer(a))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
  mvc.perform(delete("/user/"+b.id+"/follow").header("Authorization",bearer(a))).andExpect(status().isOk()).andExpect(jsonPath("$.seguidores").value(0));
 }
 @Test void likesUseAuthenticatedIdentityAndPreserveAuthor() throws Exception {
  var author=account();var reader=account();int id=create(author);
  for(int i=0;i<2;i++)mvc.perform(put("/post/"+id+"/like").header("Authorization",bearer(reader))).andExpect(status().isOk()).andExpect(jsonPath("$.curtida").value(1)).andExpect(jsonPath("$.curtido").value(true)).andExpect(jsonPath("$.autor.id").value(author.id));
  mvc.perform(put("/post/"+id).header("Authorization",bearer(reader)).contentType(MediaType.APPLICATION_JSON).content(postBody("Ataque"))).andExpect(status().isForbidden());
  mvc.perform(delete("/post/"+id).header("Authorization",bearer(reader))).andExpect(status().isForbidden());
  mvc.perform(put("/post/"+id).header("Authorization",bearer(author)).contentType(MediaType.APPLICATION_JSON).content(postBody("Editado"))).andExpect(status().isOk()).andExpect(jsonPath("$.curtida").value(1));
  mvc.perform(delete("/post/"+id+"/like").header("Authorization",bearer(reader))).andExpect(status().isOk()).andExpect(jsonPath("$.curtida").value(0));
 }
 @Test void feedIsPagedAndDeletionCleansNotificationsAndCounters() throws Exception {
  var a=account();var b=account();mvc.perform(put("/user/"+a.id+"/follow").header("Authorization",bearer(b))).andExpect(status().isOk());
  int first=create(a);int second=create(a);
  mvc.perform(get("/post/feed?size=1").header("Authorization",bearer(a))).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(second)).andExpect(jsonPath("$.hasNext").value(true));
  mvc.perform(get("/post/feed?size=500").header("Authorization",bearer(a))).andExpect(status().isBadRequest());
  mvc.perform(delete("/post/"+first).header("Authorization",bearer(a))).andExpect(status().isNoContent());
  mvc.perform(get("/notificacao/").header("Authorization",bearer(b))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
  mvc.perform(get("/user/userLogado").header("Authorization",bearer(a))).andExpect(jsonPath("$.posts").value(1));
 }
 @Test void photoUpdatesImmediatelyWithoutPuttingBase64InProfileJson() throws Exception {
  var a=account();byte[] png=Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+j3ioAAAAASUVORK5CYII=");
  var r=mvc.perform(multipart("/user/foto").file(new MockMultipartFile("file","a.png","image/png",png)).header("Authorization",bearer(a))).andExpect(status().isOk()).andExpect(jsonPath("$.fotoPerfil").value(org.hamcrest.Matchers.startsWith("/api/user/"))).andReturn();
  String url=json.readTree(r.getResponse().getContentAsString()).get("fotoPerfil").asText().substring(4);
  mvc.perform(get(url)).andExpect(status().isOk()).andExpect(content().bytes(png));
  mvc.perform(multipart("/user/foto").file(new MockMultipartFile("file","a.svg","image/svg+xml","<svg>attack</svg>".getBytes())).header("Authorization",bearer(a))).andExpect(status().isBadRequest());
  mvc.perform(get(url)).andExpect(content().bytes(png));
 }
 @Test void pdfIsValidatedAndDownloadedSeparately() throws Exception {
  var a=account();byte[] pdf="%PDF-1.4 test attachment".getBytes();
  var result=mvc.perform(multipart("/post/insert").file(new MockMultipartFile("post","","application/json",postBody("PDF").getBytes())).file(new MockMultipartFile("file","test.pdf","application/pdf",pdf)).header("Authorization",bearer(a))).andExpect(status().isCreated()).andExpect(jsonPath("$.temArquivo").value(true)).andExpect(jsonPath("$.arquivoPublicacao").doesNotExist()).andReturn();
  int id=json.readTree(result.getResponse().getContentAsString()).get("id").asInt();
  mvc.perform(get("/post/"+id+"/arquivo").header("Authorization",bearer(a))).andExpect(status().isOk()).andExpect(content().bytes(pdf));
 }
}
