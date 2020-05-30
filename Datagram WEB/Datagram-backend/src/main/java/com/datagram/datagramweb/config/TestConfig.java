package com.datagram.datagramweb.config;

import com.datagram.datagramweb.Models.Postagem;
import com.datagram.datagramweb.Models.Usuario;
import com.datagram.datagramweb.Repositories.PostagemRepository;
import com.datagram.datagramweb.Repositories.UsuarioRepository;
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

	@Override
	public void run(String... args) throws Exception {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		
		Usuario usuario1 = new Usuario(null,"rafa sanzio","1234","rafa@gmail.com","478.393.188-78",
				"ensino superior","membro");

		Usuario usuario2 = new Usuario(null,"renan abbade","1234","renan@gmail.com","345.909.654-02",
				"mestrado","membro");

		usuarioRepository.saveAll(Arrays.asList(usuario1,usuario2));

		Postagem postagem1 = new Postagem(null,usuario1,"Artigo publicado por volta de ...",
				"muito bacana!",sdf.parse("02/08/1970"),0);

		Postagem postagem2 = new Postagem(null,usuario1,"Pessoal boa tarde!, irei publicar nesta semana ...",
				"estou ansioso para a nova versao",sdf.parse("02/08/1970"),0);

		postagemRepository.saveAll(Arrays.asList(postagem1,postagem2));
		System.out.println("salvei");

	}
}
