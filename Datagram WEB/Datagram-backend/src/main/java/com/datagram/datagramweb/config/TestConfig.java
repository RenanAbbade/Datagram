/*

package com.datagram.datagramweb.config;

import com.datagram.datagramweb.Models.Comentario;
import com.datagram.datagramweb.Models.Postagem;
import com.datagram.datagramweb.Models.Usuario;
import com.datagram.datagramweb.Repositories.PostagemRepository;
import com.datagram.datagramweb.Repositories.UsuarioRepository;
import com.datagram.datagramweb.Services.PostagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.text.SimpleDateFormat;

import java.util.Arrays;

@Configuration
@Profile("test")
public class TestConfig implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PostagemRepository postagemRepository;

    @Autowired
    private PostagemService postagemService;

    @Override
    public void run(String... args) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // USUARIOS PARA TESTE
        Usuario usuario1 = new Usuario(null,"rafael de lima","1234","rafa@gmail.com","478.393.188-78",
                "ensino superior","membro");

        Usuario usuario2 = new Usuario(null,"renan abbade","1234","renan@gmail.com","345.909.654-02",
                "mestrado","membro");

        Usuario usuario3 = new Usuario(null,"rafael vasques abbade","1234","rafaellima@gmail.com","652.785.000-22",
                "doutorado","membro");

        Usuario usuario4 = new Usuario(null,"gabriel caskolino","1234","gabriel@gmail.com","246.567.194-76",
                "mestre","membro");

        usuario1.setIdsSeguindo(usuario2.getId());

        System.out.println("Seguidores --> "+usuario2.getSeguidores());


        usuarioRepository.saveAll(Arrays.asList(usuario1,usuario2,usuario3));

        //COMENTARIOS PARA TESTE
        Comentario comentario1 = new Comentario(usuario2,"muito bom parabens",sdf.parse("02/08/1970"));
        Comentario comentario2 = new Comentario(usuario2,"belo artigo!",sdf.parse("20/11/2020"));
        Comentario comentario3 = new Comentario(usuario3,"BACANAO MEU AMIGO!",sdf.parse("20/11/2020"));
        Comentario comentario4 = new Comentario(usuario2,"BELOS ARGUMENTOS",sdf.parse("20/11/2020"));

        //POSTAGEMS PARA TESTE
        Postagem postagem1 = new Postagem(null,usuario1,"tituloPostagem","subtituloPostagem","conteudo",
                "02/08/1970",0);

        Postagem postagem2 = new Postagem(null,usuario1,"tituloPostagem2","subtituloPostagem2","conteudo2",
                "31/12/2039",0);

        //POSTAGEM OBTEM COMENTARIOS
//        postagem1.getComentario().addAll(Arrays.asList(comentario1,comentario3));
//        postagem2.getComentario().addAll(Arrays.asList(comentario3,comentario4));

//        System.out.println("RESULTADO --> "+postagemService.containsPost(usuario1,1));

        postagemRepository.saveAll(Arrays.asList(postagem1,postagem2));
    }
}
*/