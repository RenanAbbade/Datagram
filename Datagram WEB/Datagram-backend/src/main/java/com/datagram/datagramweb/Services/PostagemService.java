package com.datagram.datagramweb.Services;

import com.datagram.datagramweb.Models.Postagem;
import com.datagram.datagramweb.Models.Usuario;
import com.datagram.datagramweb.Repositories.PostagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class PostagemService {

    @Autowired
    private PostagemRepository repo;

    @Autowired
    UsuarioService serviceUsuario;


  // CREATE
    public Postagem insert(Postagem obj) {
        obj.setId(null);
        obj.setAutor(UsuarioService.usuarioLogado);
        return repo.save(obj);
    }

    //find all
    public List<Postagem> findAll(){
        return repo.findAll();
    }

    public List<Postagem> findAllbyAutorId(Integer id){
        List<Postagem> list = repo.findAll();
        List<Postagem> listPostsById = new ArrayList<Postagem>();

        for(Postagem post : list){
            if(post.getAutor().getId() == id)
                listPostsById.add(post);
        }

        return listPostsById;
    }

    //find by id
    public Postagem find(Integer id){
        return repo.findById(id).get();
    }

    //update
	public Postagem update(Postagem obj) {
        Postagem postEstadoAntigo = find(obj.getId());

        if(postEstadoAntigo.getCurtida() == null){

          obj.setIdsCurtida(UsuarioService.getUsuarioLogado().getId());
        }

        else if(obj.getCurtida() > postEstadoAntigo.getCurtida()){

          obj.setIdsCurtida(UsuarioService.getUsuarioLogado().getId());
        }
        
        return repo.save(obj);
      }
    
      // DELETE
    public void delete(Integer id) {
    
        find(id);
        try {
          repo.deleteById(id);
        } catch (DataIntegrityViolationException e) {
          throw new DataIntegrityViolationException("Não é possível excluir essa entidade!");
        }
      }

    public List<Postagem> findPostsSeguidores(){

      Set<Integer> seguidores = UsuarioService.usuarioLogado.getIdsSeguindo(); // trocar este metodo por um que retorna os usuarios e nao o ID;
      List<Postagem> listPostSeguidores = new ArrayList<Postagem>();
/*
        try{
            seguidores = UsuarioService.usuarioLogado.getIdsSeguindo(); // trocar este metodo por um que retorna os usuarios e nao o ID
        }catch (NullPointerException e){
            throw new NullPointerException("***USUARIO NAO POSSUI SEGUIDORES***");
        }
*/
        for(Integer id : seguidores){
            listPostSeguidores.addAll(findAllbyAutorId(id));
        }
        return listPostSeguidores;
    }

    
}

