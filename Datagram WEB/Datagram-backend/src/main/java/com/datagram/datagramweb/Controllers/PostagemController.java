package com.datagram.datagramweb.Controllers;

import com.datagram.datagramweb.Models.Postagem;
import com.datagram.datagramweb.Services.PostagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/post")
public class PostagemController {

    @Autowired
    PostagemService postagemService;

    @GetMapping
    public ResponseEntity<List<Postagem>> findAll(){
        List<Postagem> listPostagem = postagemService.findAll();
        return ResponseEntity.ok().body(listPostagem);
    }

    @GetMapping(value ="/{id}")
    public ResponseEntity<Postagem>findById(@PathVariable Integer id){
        Postagem postagem = postagemService.findById(id);
        return ResponseEntity.ok().body(postagem);
    }
}
