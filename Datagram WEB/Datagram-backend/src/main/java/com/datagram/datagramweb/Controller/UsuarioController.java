package com.datagram.datagramweb.Controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.datagram.datagramweb.Domain.Postagem;
import com.datagram.datagramweb.Domain.Usuario;
import com.datagram.datagramweb.Service.UsuarioService;
import com.datagram.datagramweb.Service.validation.UsuarioValidator;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@CrossOrigin
@RestController
@RequestMapping(value = "/user")
public class UsuarioController {

    private final UsuarioService service;
    private final UsuarioValidator validator;

    public UsuarioController(UsuarioService service, UsuarioValidator validator) {
        this.service = service;
        this.validator = validator;
    }

    // GET id
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> find(@PathVariable Integer id) {
        Usuario obj = service.find(id);
        return ResponseEntity.ok().body(obj);
    }

    @GetMapping("/userLogado")
    public ResponseEntity<Usuario> findAuthUser() {
        Usuario obj = service.authenticated();
        return ResponseEntity.ok().body(obj);
    }

    @PostMapping("/insert")
    @CrossOrigin
    public ResponseEntity<Void> insert(@RequestBody Usuario obj) {

        if (!service.validaExistenciaEmail(obj.getEmail())) {

            if (obj.getTipoUsuario().equalsIgnoreCase("Pesquisador"))
                if (validator.isGreatherThan18(obj.getDataNasc(), obj.getDataInicio()))
                    obj = service.insert(obj);

                else
                    return ResponseEntity.badRequest().build();

            else if (validator.isCPF(obj.getCpf())) {
                if (validator.isGreatherThan18(obj.getDataNasc()))
                    obj = service.insert(obj);
                else
                    return ResponseEntity.badRequest().build();
            } else
                return ResponseEntity.badRequest().build();

        } else
            return ResponseEntity.badRequest().build();

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getId()).toUri();

        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Usuario obj) {

        Map<String, String> response = service.login(obj.getEmail(), obj.getSenha());
        return ResponseEntity.ok(response);
    }

    // UPDATE usuario logado
    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Usuario obj) {
        obj = service.update(obj);
        return ResponseEntity.noContent().build();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // OBTEM TODOS OS POSTS DE UM USUARIO
    @GetMapping("/{id}/posts")
    public ResponseEntity<List<Postagem>> findPosts(@PathVariable Integer id) {
        Usuario usuario = service.find(id);
        return ResponseEntity.ok().body(usuario.getPostagem());
    }

    // consulte por user/nome/?nome_value=rafael
    @GetMapping("/nome")
    public ResponseEntity<List<Usuario>> findByNome(@RequestParam String nome_value) {
        List<Usuario> usuarios = service.findByNome(nome_value);
        return ResponseEntity.ok().body(usuarios);
    }

    @GetMapping("/instituicao")
    public ResponseEntity<List<Usuario>> findByInstituicao(@RequestParam String instituicao_value) {
        List<Usuario> usuario = service.findByInstituicao(instituicao_value);
        return ResponseEntity.ok().body(usuario);
    }

    @GetMapping("/seguindo/{id}")
    public ResponseEntity<Boolean> validFollower(@PathVariable Integer id) {
        Usuario authenticatedUser = service.authenticated();
        Boolean resposta = authenticatedUser.getIdsSeguindo().contains(id);
        return ResponseEntity.ok(resposta);

    }

    @GetMapping("/seguidores")
    public ResponseEntity<List<Usuario>> findAllSeguidores() {
        Usuario authenticatedUser = service.authenticated();
        List<Usuario> list = new ArrayList<>();
        for (Integer id : authenticatedUser.getIdsSeguidores()) {
            list.add(service.find(id));
        }
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/seguindo")
    public ResponseEntity<List<Usuario>> findAllSeguindo() {
        Usuario authenticatedUser = service.authenticated();
        List<Usuario> list = new ArrayList<>();
        for (Integer id : authenticatedUser.getIdsSeguindo()) {
            list.add(service.find(id));
        }
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/interesses")
    public ResponseEntity<List<Usuario>> findUsersByInteresses() {
        List<Usuario> usuariosMutuos = service.findUsersByInteresses();
        return ResponseEntity.ok().body(usuariosMutuos);
    }

}
