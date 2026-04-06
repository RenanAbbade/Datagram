package com.datagram.datagramweb.Service;

import com.datagram.datagramweb.Domain.Postagem;
import com.datagram.datagramweb.Repository.PostagemRepository;
import com.datagram.datagramweb.Repository.UsuarioRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PostagemService {

    private final PostagemRepository repo;
    private final UsuarioRepository uRepo;
    private final UsuarioService usuarioService;

    public PostagemService(PostagemRepository repo, UsuarioRepository uRepo, UsuarioService usuarioService) {
        this.repo = repo;
        this.uRepo = uRepo;
        this.usuarioService = usuarioService;
    }

    // CREATE
    public Postagem insert(Postagem obj) {
        obj.setId(null);
        obj.setAutor(usuarioService.authenticated());

        usuarioService.authenticated().setPosts(1);

        uRepo.save(usuarioService.authenticated());

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
                .removeIf(x -> x.equals(usuarioService.authenticated().getId())) ? true : false;

        if (postEstadoAntigo.getCurtida() == null) {
            obj.setIdsCurtida(usuarioService.authenticated().getId());
        } else if (obj.getCurtida() > postEstadoAntigo.getCurtida()) {
            obj.setAllIdsCurtida(postEstadoAntigo.getIdsCurtida());// reinserindo as curtidas anteriores
            if (!descurtida)// Se descurtida for false, a ação é uma curtida
                obj.setIdsCurtida(usuarioService.authenticated().getId());
        }

        return repo.save(obj);
    }


    // DELETE
    public void delete(Integer id) {

        find(id);
        try {
            repo.deleteById(id);
            usuarioService.authenticated().setPosts(-1);
            uRepo.save(usuarioService.authenticated());
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Não é possível excluir essa entidade!");
        }
    }

    // FEED
    public List<Postagem> findPostsSeguidores() {
        Set<Integer> seguidores = usuarioService.authenticated().getIdsSeguindo();
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
        Integer id = usuarioService.authenticated().getId();
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
