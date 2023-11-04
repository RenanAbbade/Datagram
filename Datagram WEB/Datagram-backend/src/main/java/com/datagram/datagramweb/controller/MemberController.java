package com.datagram.datagramweb.controller;

import com.datagram.datagramweb.domain.Member;
import com.datagram.datagramweb.domain.Postagem;
import com.datagram.datagramweb.dto.MemberDTO;
import com.datagram.datagramweb.service.UsuarioService;
import com.datagram.datagramweb.service.validation.UsuarioValidator;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/user")
public class MemberController {

  @Autowired
  UsuarioService service;

  @Autowired
  UsuarioValidator validator;

  // GET id
  @GetMapping(value = "/{id}")
  public ResponseEntity<Member> find(@PathVariable Integer id) {
    Member obj = service.find(id);
    return ResponseEntity.ok().body(obj);
  }
//TODO JWT
//  @GetMapping(value = "/userLogado")
//  public ResponseEntity<Usuario> findAuthUser() {
//    Usuario obj = UsuarioService.getUsuarioLogado();
//    return ResponseEntity.ok().body(obj);
//  }

  @PostMapping
  @CrossOrigin
  @RequestMapping(value = "/insert")
  public ResponseEntity<MemberDTO> insert(@RequestBody  MemberDTO memberDTOrequest) {

    MemberDTO memberDTO = service.save(memberDTOrequest);

    URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(memberDTO).toUri();

    return ResponseEntity.created(uri).body(memberDTO);
  }

  @PostMapping
  @RequestMapping(value = "/login")
  @Transactional
  public ResponseEntity<String> login(@RequestBody Member obj) {
    Member member = service.login(obj.getEmail(), obj.getSenha());
    if (member == null)
      return ResponseEntity.ok("NoAuth");
    return ResponseEntity.ok("auth");
  }

  // UPDATE usuario logado
  @PutMapping(value = "/")
  @Transactional
  public ResponseEntity<Member> update(@RequestBody Member updatedMember) {
    return ResponseEntity.ok(service.update(updatedMember));
  }

  // DELETE
  @DeleteMapping(value = "{id}")
  @Transactional
  public ResponseEntity<Member> delete(@PathVariable Integer id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  // OBTEM TODOS OS POSTS DE UM USUARIO
  @GetMapping(value = "{id}/posts")
  public ResponseEntity<List<Postagem>> findPosts(@PathVariable Integer id) {
    Member member = service.find(id);
    return ResponseEntity.ok().body(member.getPostagem());
  }

  // consulte por user/nome/?nome_value=rafael
  @GetMapping(value = "/name")
  public ResponseEntity<List<Member>> findByNome(@RequestParam String nome_value) {
    List<Member> members = service.findByNome(nome_value);
    return ResponseEntity.ok().body(members);
  }

  @GetMapping(value = "/university")
  public ResponseEntity<List<Member>> findByInstituicao(@RequestParam String instituicao_value) {
    List<Member> member = service.findByInstituicao(instituicao_value);
    return ResponseEntity.ok().body(member);
  }

  @GetMapping(value = "/seguindo/{id}")
  public ResponseEntity<Boolean> validFollower(@PathVariable Integer id) {
    Boolean resposta = (UsuarioService.memberLogado.getIdsSeguindo().contains(id)) ? true : false;
    return ResponseEntity.ok(resposta);

  }

  @GetMapping(value = "/seguidores")
  public ResponseEntity<List<Member>> findAllSeguidores() {
    List<Member> list = new ArrayList<>();
    for (Integer id : UsuarioService.memberLogado.getIdsSeguidores()) {
      list.add(service.find(id));
    }
    return ResponseEntity.ok().body(list);
  }

  @GetMapping(value = "/seguindo")
  public ResponseEntity<List<Member>> findAllSeguindo() {
    List<Member> list = new ArrayList<>();
    for (Integer id : UsuarioService.memberLogado.getIdsSeguindo()) {
      list.add(service.find(id));
    }
    return ResponseEntity.ok().body(list);
  }

  @GetMapping(value = "/interesses")
  public ResponseEntity<List<Member>> findUsersByInteresses() {
    List<Member> usuariosMutuos = service.findUsersByInteresses();
    return ResponseEntity.ok().body(usuariosMutuos);
  }

}
