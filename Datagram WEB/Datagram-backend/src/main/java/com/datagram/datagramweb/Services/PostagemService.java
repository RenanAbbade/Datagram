package com.datagram.datagramweb.Services;

import com.datagram.datagramweb.Models.Postagem;
import com.datagram.datagramweb.Repositories.PostagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import java.util.List;

@Service
public class PostagemService {

    @Autowired
    private PostagemRepository postagemRepository;

    public List<Postagem> findAll(){
        return postagemRepository.findAll();
    }

    public Postagem findById(Integer id){
        return postagemRepository.findById(id).get();
    }
}
