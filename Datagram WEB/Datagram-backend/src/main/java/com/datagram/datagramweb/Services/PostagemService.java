package com.datagram.datagramweb.Services;

import com.datagram.datagramweb.Models.Postagem;
import com.datagram.datagramweb.Repositories.PostagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PostagemService {

    @Autowired
    private PostagemRepository repo;


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
            if(post.getId() == id)
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
        find(obj.getId());
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

    
}
