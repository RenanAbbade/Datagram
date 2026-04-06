package com.datagram.datagramweb.Controller;

import com.datagram.datagramweb.Domain.Postagem;
import com.datagram.datagramweb.Service.NotificacaoService;
import com.datagram.datagramweb.Service.PostagemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/post")
public class PostagemController {

    private final PostagemService service;
    private final NotificacaoService nService;

    public PostagemController(PostagemService service, NotificacaoService nService) {
        this.service = service;
        this.nService = nService;
    }

    @GetMapping
    public ResponseEntity<List<Postagem>> findAll() {
        List<Postagem> listPostagem = service.findAll();
        return ResponseEntity.ok().body(listPostagem);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Postagem>> findAllbyId(@PathVariable Integer id) {
        List<Postagem> listPostagem = service.findAllbyAutorId(id);
        Collections.reverse(listPostagem);// Reverto a ordem de apresentacao para mostrar da postagem mais recente
        return ResponseEntity.ok().body(listPostagem);
    }

    @PostMapping("/insert")
    @CrossOrigin
    public ResponseEntity<Void> insert(@RequestBody Postagem obj) {

        obj = service.insert(obj);

        nService.createNotify(obj);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getId()).toUri();

        return ResponseEntity.created(uri).build();
    }

    // UPDATE
    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Postagem obj) {
        service.update(obj);
        return ResponseEntity.noContent().build();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/feed")
    public ResponseEntity<List<Postagem>> findPostsSeguidores() {
        List<Postagem> postsSeguidores = service.findPostsSeguidores();
        return ResponseEntity.ok().body(postsSeguidores);
    }

    @GetMapping("/maisCurtido")
    public ResponseEntity<List<Postagem>> findPostsMaisCurtidos() {
        List<Postagem> postsPopulares = service.findPostsMaisCurtidos();
        return ResponseEntity.ok().body(postsPopulares);
    }
}
