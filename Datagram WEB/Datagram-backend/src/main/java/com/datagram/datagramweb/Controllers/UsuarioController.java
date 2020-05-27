package com.datagram.datagramweb.Controllers;

import java.net.URI;

import com.datagram.datagramweb.Models.Usuario;
import com.datagram.datagramweb.Services.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;


  @CrossOrigin
  @RestController
  @RequestMapping(value="/")
  public class UsuarioController {

    @Autowired
    UsuarioService service;

    //GET Index
    
    @GetMapping
    public ResponseEntity<String> index() {
        return ResponseEntity.ok().body("Index da app");
    }

        //GET id
    @GetMapping(value="/{id}")
	  public ResponseEntity<Usuario> find(@PathVariable Integer id) {
      Usuario obj = service.find(id);
      return ResponseEntity.ok().body(obj);
	}

    @PostMapping
    @CrossOrigin
    @RequestMapping(value="/insert")
    public ResponseEntity<String> insert(@RequestBody Usuario obj) {
      
      obj = service.insert(obj);

      URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getId()).toUri();
        
      ResponseEntity.created(uri).build();
      
      return ResponseEntity.ok("created");
    } 

    @PostMapping
    @RequestMapping(value="/login")
    public ResponseEntity<Usuario>login(@RequestBody Usuario obj) {
    Usuario usuario = service.login(obj.getEmail());
    return ResponseEntity.ok().body(usuario);
    } 
    
    //UPDATE
    @PutMapping(value="{id}")
    public ResponseEntity<Void> update(@RequestBody Usuario obj, @PathVariable Integer id){
      obj.setId(id);//Garantia do objeto, vai ser trocado por DTO no futuro.
      obj = service.update(obj);
      return ResponseEntity.noContent().build();
    }

    //DELETE
    @DeleteMapping(value = "{id}")
    public ResponseEntity<Usuario> delete(@PathVariable Integer id) {
      service.delete(id);
      return ResponseEntity.noContent().build();
	}

}