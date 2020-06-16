package com.datagram.datagramweb.Controllers;

import com.datagram.datagramweb.Models.Postagem;
import com.datagram.datagramweb.Models.Usuario;
import com.datagram.datagramweb.Services.PostagemService;
import com.datagram.datagramweb.Services.UsuarioService;

import com.datagram.datagramweb.Services.UsuarioService;
import javafx.geometry.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/post")
public class PostagemController {

    @Autowired
    PostagemService service;


    @GetMapping
    public ResponseEntity<List<Postagem>> findAll(){
        List<Postagem> listPostagem = service.findAll();
        return ResponseEntity.ok().body(listPostagem);
    }

    @RequestMapping(value="/{id}", method=RequestMethod.GET)
    public ResponseEntity<List<Postagem>> findAllbyId(@PathVariable Integer id){
        List<Postagem> listPostagem = service.findAllbyAutorId(id);
        Collections.reverse(listPostagem);//Reverto a ordem de apresentacao para mostrar da postagem mais recente
        return ResponseEntity.ok().body(listPostagem);
    }
/*
    @GetMapping(value ="/{id}")
    public ResponseEntity<Postagem>findById(@PathVariable Integer id){
        Postagem postagem = service.find(id);
        return ResponseEntity.ok().body(postagem);
    }

*/
    @PostMapping
    @CrossOrigin
    @RequestMapping(value = "/insert")
    public ResponseEntity<String> insert(@RequestBody Postagem obj) {
        
        obj = service.insert(obj);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getId()).toUri();

        ResponseEntity.created(uri).build();

        return ResponseEntity.ok("CREATED");
    }

    //UPDATE
    @RequestMapping(value="/", method=RequestMethod.PUT)
    public ResponseEntity<Void> update(@RequestBody Postagem obj) {
        obj = service.update(obj);
        return ResponseEntity.noContent().build();
    }

    //DELETE
    @RequestMapping(value="/{id}", method=RequestMethod.DELETE)
    public ResponseEntity<Postagem> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/feed")
    public ResponseEntity<List<Postagem>> findPostsSeguidores() {
        List<Postagem> postsSeguidores = service.findPostsSeguidores();

        return ResponseEntity.ok().body(postsSeguidores);
    }
}
