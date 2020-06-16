package com.datagram.datagramweb.Controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.datagram.datagramweb.Models.Postagem;
import com.datagram.datagramweb.Models.Usuario;
import com.datagram.datagramweb.Services.UsuarioService;
import com.datagram.datagramweb.Services.validation.UsuarioValidator;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@CrossOrigin
@RestController
@RequestMapping(value = "/user")
public class UsuarioController {

  @Autowired
  UsuarioService service;

  @Autowired
  UsuarioValidator validator;

  // GET id
  @GetMapping(value = "/{id}")
  public ResponseEntity<Usuario> find(@PathVariable Integer id) {
    Usuario obj = service.find(id);
    return ResponseEntity.ok().body(obj);
  }

  @GetMapping(value = "/userLogado")
  public ResponseEntity<Usuario> findAuthUser() {
    Usuario obj = UsuarioService.getUsuarioLogado();
    return ResponseEntity.ok().body(obj);
  }

  @PostMapping
  @CrossOrigin
  @RequestMapping(value = "/insert")
  public ResponseEntity<String> insert(@RequestBody Usuario obj) {

    if (!service.validaExistenciaEmail(obj.getEmail())) {

      if (obj.getTipoUsuario().equalsIgnoreCase("Pesquisador"))
        if (validator.isGreatherThan18(obj.getDataNasc(), obj.getDataInicio()))
          obj = service.insert(obj);

        else
          return ResponseEntity.ok("DATE");

      else if (validator.isCPF(obj.getCpf())) {
        if (validator.isGreatherThan18(obj.getDataNasc()))
          obj = service.insert(obj);
        else
          return ResponseEntity.ok("DATE");
      } else
        return ResponseEntity.ok("CPF");

    } else
      return ResponseEntity.ok("EMAIL");


    URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getId()).toUri();

    ResponseEntity.created(uri).build();

    return ResponseEntity.ok("CREATED");
  }

  @PostMapping
  @RequestMapping(value = "/login")
  public ResponseEntity<String> login(@RequestBody Usuario obj) {
    Usuario usuario = service.login(obj.getEmail(), obj.getSenha());
    if(usuario == null) {
      return ResponseEntity.ok("NoAuth");
    }
    //return ResponseEntity.ok().body(usuario);
    return ResponseEntity.ok("auth");
  }

  //UPDATE usuario logado
  @PutMapping(value = "/")
  public ResponseEntity<Void> update(@RequestBody Usuario obj) {
    obj = service.update(obj);
    return ResponseEntity.noContent().build();
  }

  //DELETE
  @DeleteMapping(value = "{id}")
  public ResponseEntity<Usuario> delete(@PathVariable Integer id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  //OBTEM TODOS OS POSTS DE UM USUARIO
  @GetMapping(value = "{id}/posts")
  public ResponseEntity<List<Postagem>> findPosts(@PathVariable Integer id) {
    Usuario usuario = service.find(id);
    return ResponseEntity.ok().body(usuario.getPostagem());
  }

  // consulte por user/nome/?nome_value=rafael
  @GetMapping(value = "/nome")
  public ResponseEntity<List<Usuario>> findByNome(@RequestParam String nome_value){
    List<Usuario> usuarios = service.findByNome(nome_value);
    return ResponseEntity.ok().body(usuarios);
  }

  @GetMapping(value = "/instituicao")
  public ResponseEntity<List<Usuario>> findByInstituicao(@RequestParam String instituicao_value){
    List<Usuario> usuario = service.findByInstituicao(instituicao_value);
    return ResponseEntity.ok().body(usuario);
  }

  @GetMapping(value = "/seguindo/{id}")
  public ResponseEntity<Boolean> validFollower(@PathVariable Integer id){
    Boolean resposta = (UsuarioService.usuarioLogado.getIdsSeguindo().contains(id)) ? true : false;
    return ResponseEntity.ok(resposta);

  }

  @GetMapping(value = "/seguidores")
  public ResponseEntity <List<Usuario>> findAllSeguidores(){
    List<Usuario> list = new ArrayList<>();
    for(Integer id: UsuarioService.usuarioLogado.getIdsSeguidores()){
      list.add(service.find(id));
    }
    return ResponseEntity.ok().body(list);
}

  @GetMapping(value = "/seguindo")
  public ResponseEntity <List<Usuario>> findAllSeguindo(){
    List<Usuario> list = new ArrayList<>();
    for(Integer id: UsuarioService.usuarioLogado.getIdsSeguindo()){
      list.add(service.find(id));
    }
    return ResponseEntity.ok().body(list);
  }
}








