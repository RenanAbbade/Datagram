package com.datagram.datagramweb.service;

import com.datagram.datagramweb.domain.Postagem;
import com.datagram.datagramweb.repository.MemberRepository;
import com.datagram.datagramweb.repository.PostagemRepository;
import com.datagram.datagramweb.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PostagemService {

    @Autowired
    private PostagemRepository repo;

    @Autowired
    private MemberRepository uRepo;

    // CREATE
    public Postagem insert(Postagem obj) {
        obj.setId(null);
        obj.setAutor(UsuarioService.memberLogado);

        UsuarioService.memberLogado.setPosts(1);

        uRepo.save(UsuarioService.memberLogado);

        return repo.save(obj);
    }

    // find by id
    public Postagem find(Integer id) {
        return repo.findById(id).get();
    }


    // find all
    public List<Postagem> findAll() {
        return repo.findAll();
    }

    public List<Postagem> findAllbyAutorId(Integer id) {
        List<Postagem> list = repo.findAll();
        List<Postagem> listPostsById = new ArrayList<Postagem>();

        for (Postagem post : list) {
            if (post.getAutor().getId() == id)
                listPostsById.add(post);
        }
        return listPostsById;
    }

    // update
    public Postagem update(Postagem obj) {
        Postagem postEstadoAntigo = find(obj.getId());
        // Se o Id do usuário logado estiver entre o set de curtidas da postagem,
        // removo, caracterizando deslike.
        Boolean descurtida = postEstadoAntigo.getIdsCurtida()
                .removeIf(x -> x.equals(UsuarioService.getUsuarioLogado().getId())) ? true : false;

        if (postEstadoAntigo.getCurtida() == null) {
            obj.setIdsCurtida(UsuarioService.getUsuarioLogado().getId());
        } else if (obj.getCurtida() > postEstadoAntigo.getCurtida()) {
            obj.setAllIdsCurtida(postEstadoAntigo.getIdsCurtida());// reinserindo as curtidas anteriores
            if (!descurtida)// Se descurtida for false, a ação é uma curtida
                obj.setIdsCurtida(UsuarioService.getUsuarioLogado().getId());
        }

        return repo.save(obj);
    }


    // DELETE
    public void delete(Integer id) {

        find(id);
        try {
            repo.deleteById(id);
            UsuarioService.memberLogado.setPosts(-1);
            uRepo.save(UsuarioService.memberLogado);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Não é possível excluir essa entidade!");
        }
    }

    // FEED
    public List<Postagem> findPostsSeguidores() {
        Set<Integer> seguidores = UsuarioService.memberLogado.getIdsSeguindo();
        List<Postagem> listPostSeguidores = new ArrayList<Postagem>();
        for (Integer id : seguidores) {
            listPostSeguidores.addAll(findAllbyAutorId(id));
        }
        Collections.reverse(sortPostByData(listPostSeguidores));
        return listPostSeguidores;
    }

    public List<Postagem> sortPostByData(List<Postagem> myList) {
        myList.sort(new Comparator<Postagem>() {
            @Override
            public int compare(Postagem postagem1, Postagem postagem2) {
                if (postagem1.getDate() == null || postagem2.getDate() == null)
                    return 0;
                return postagem1.getDate().compareTo(postagem2.getDate());
            }
        });
        return myList;
    }

    public List<Postagem> findPostsMaisCurtidos() {
        Integer id = UsuarioService.memberLogado.getId();
        List<Postagem> listPost = new ArrayList<Postagem>(findAllbyAutorId(id));
        Collections.reverse(sortPostByCurtida(listPost));
        return listPost;
    }

    public List<Postagem> sortPostByCurtida(List<Postagem> myList) {
        myList.sort(new Comparator<Postagem>() {
            @Override
            public int compare(Postagem postagem1, Postagem postagem2) {
                if (postagem1.getCurtida() == null || postagem2.getCurtida() == null)
                    return 0;
                return postagem1.getCurtida().compareTo(postagem2.getCurtida());
            }
        });
        return myList;
    }

}

