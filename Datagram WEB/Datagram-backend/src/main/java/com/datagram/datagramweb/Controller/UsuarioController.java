package com.datagram.datagramweb.Controller;
import com.datagram.datagramweb.api.ApiDtos.*;
import com.datagram.datagramweb.Domain.Usuario;
import com.datagram.datagramweb.Service.*;
import com.datagram.datagramweb.Service.validation.UsuarioValidator;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.net.URI;

@RestController @RequestMapping("/user")
@org.springframework.transaction.annotation.Transactional
public class UsuarioController {
 private final UsuarioService users; private final PostagemService posts; private final UsuarioValidator validator;
 @PostMapping("/login") public Session login(@Valid @RequestBody Login data) {
  var token=users.login(data.email(),data.senha()).get("token");
  return new Session(token,UserView.of(users.findByEmail(data.email().trim().toLowerCase(Locale.ROOT)),true));
 }
 @PostMapping("/insert") public ResponseEntity<UserView> register(@Valid @RequestBody Register r){
  if(!Set.of("Membro","Pesquisador").contains(r.tipoUsuario())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tipo de usuário inválido");
  if(users.validaExistenciaEmail(r.email().trim().toLowerCase(Locale.ROOT))) throw new ResponseStatusException(HttpStatus.CONFLICT,"Email já cadastrado");
  boolean adult;
  try { adult=validator.isGreatherThan18(r.dataNasc());
   if("Pesquisador".equals(r.tipoUsuario())) adult=adult && validator.isGreatherThan18(r.dataNasc(),r.dataInicio());
  } catch(RuntimeException e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Datas inválidas");}
  if(!adult) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"É necessário ter 18 anos e informar datas válidas");
  if("Membro".equals(r.tipoUsuario()) && (r.cpf()==null || !validator.isCPF(r.cpf()))) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"CPF inválido");
  Usuario u=new Usuario();u.setNome(r.nome().trim());u.setEmail(r.email());u.setSenha(r.senha());u.setDataNasc(r.dataNasc());
  u.setCpf(r.cpf());u.setTipoUsuario(r.tipoUsuario());u.setEscolaridade(r.escolaridade());u.setEstado(r.estado());u.setMunicipio(r.municipio());
  u.setInstituicao(r.instituicao());u.setLinkCv(UsuarioService.safeUrl(r.linkCv()));u.setDataInicio(r.dataInicio());
  u=users.insert(u);return ResponseEntity.created(URI.create("/user/"+u.getId())).body(UserView.of(u,true));
 }
 @GetMapping("/userLogado") public UserView me(){return UserView.of(users.authenticated(),true);}
 @GetMapping("/{id}") public UserView user(@PathVariable Integer id){return UserView.of(users.find(id),users.authenticated().getId().equals(id));}
 @PutMapping({"","/"}) public UserView update(@Valid @RequestBody Profile data){return UserView.of(users.updateProfile(data),true);}
 @PutMapping("/{id}/follow") public UserView follow(@PathVariable Integer id){return UserView.of(users.follow(id,true),false);}
 @DeleteMapping("/{id}/follow") public UserView unfollow(@PathVariable Integer id){return UserView.of(users.follow(id,false),false);}
 @GetMapping("/seguindo/{id}") public boolean following(@PathVariable Integer id){return users.checkFollower(id);}
 @GetMapping("/seguidores") public List<UserView> followers(){return users.authenticated().getIdsSeguidores().stream().map(users::find).map(u->UserView.of(u,false)).toList();}
 @GetMapping("/seguindo") public List<UserView> following(){return users.authenticated().getIdsSeguindo().stream().map(users::find).map(u->UserView.of(u,false)).toList();}
 @GetMapping("/interesses") public List<UserView> suggestions(){return users.findUsersByInteresses().stream().map(u->UserView.of(u,false)).toList();}
 @GetMapping("/nome") public List<UserView> byName(@RequestParam String nome_value){return users.findByNome(nome_value).stream().map(u->UserView.of(u,false)).limit(50).toList();}
 @GetMapping("/instituicao") public List<UserView> byInstitution(@RequestParam String instituicao_value){return users.findByInstituicao(instituicao_value).stream().map(u->UserView.of(u,false)).limit(50).toList();}
 @GetMapping("/{id}/posts") public List<PostView> posts(@PathVariable Integer id){return posts.findAllbyAutorId(id).stream().map(p->PostView.of(p,users.authenticated().getId())).toList();}
 @PostMapping(value="/foto",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 public UserView photo(@RequestParam("file") MultipartFile file) throws java.io.IOException {
  String data=com.datagram.datagramweb.api.Uploads.image(file);
  Usuario u=users.authenticated();u.setFotoPerfil(data);photoRepo.save(u);return UserView.of(u,true);
 }
 private final com.datagram.datagramweb.Repository.UsuarioRepository photoRepo;
 @org.springframework.beans.factory.annotation.Autowired
 public UsuarioController(UsuarioService u,PostagemService p,UsuarioValidator v,com.datagram.datagramweb.Repository.UsuarioRepository repo){users=u;posts=p;validator=v;photoRepo=repo;}
 @GetMapping("/{id}/foto") public ResponseEntity<byte[]> photo(@PathVariable Integer id){
  String value=users.find(id).getFotoPerfil();if(value==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Foto não encontrada");
  int comma=value.indexOf(','); String type=value.substring(5,value.indexOf(';'));
  return ResponseEntity.ok().contentType(MediaType.parseMediaType(type)).cacheControl(CacheControl.noCache()).body(Base64.getDecoder().decode(value.substring(comma+1)));
 }
}
