package com.datagram.datagramweb.Services;

import com.datagram.datagramweb.Models.Postagem;
import com.datagram.datagramweb.Models.Usuario;
import com.datagram.datagramweb.Repositories.PostagemRepository;
import com.datagram.datagramweb.Repositories.UsuarioRepository;
import javafx.geometry.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

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

          obj.setAllIdsCurtida(postEstadoAntigo.getIdsCurtida());//reinserindo as curtidas anteriores
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

    public List<Postagem> sortPostByData(List<Postagem> myList){
        myList.sort(new Comparator<Postagem>() {
            @Override
            public int compare(Postagem postagem1, Postagem postagem2) {
                if(postagem1.getDate() == null || postagem2.getDate() == null)
                    return 0;
                return postagem1.getDate().compareTo(postagem2.getDate());
            }
        });
        return myList;
    }


    public List<Postagem> findPostsSeguidores(){
      Set<Integer> seguidores = UsuarioService.usuarioLogado.getIdsSeguindo();
      List<Postagem> listPostSeguidores = new ArrayList<Postagem>();
        for(Integer id : seguidores){
            listPostSeguidores.addAll(findAllbyAutorId(id));
        }
        Collections.reverse(sortPostByData(listPostSeguidores));
        return listPostSeguidores;
    }


}

