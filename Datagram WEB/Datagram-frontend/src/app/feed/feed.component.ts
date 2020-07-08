import { UsuarioLogadoService } from './../services/usuarioLogado.service';
import { UsuarioServiceService } from './../services/usuario-service.service';
import { PostServiceService } from './../services/post-service.service';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-feed',
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.css']
})
export class FeedComponent implements OnInit {

  constructor(private postService: PostServiceService, private usuarioLogadoService:  UsuarioLogadoService) { }

  postagens;

  usuario;

  // tslint:disable-next-line: max-line-length
  postagem = {id: Number, autor: '', titulo: '', subtitulo: '', conteudo: '', date: '', curtida: 0, idsCurtida: [], url: '', arquivoPublicacao: '', palavrasChave: [], tipoPostagem: ''};

  buildFeedUsuarioLogado(){
    this.postService.buildFeed().subscribe(res => {
      this.postagens = JSON.parse(JSON.stringify(res));
      console.log("aqui"+this.postagens);
    });
  }

  public editPost(id, act){
    for (let post of this.postagens) {
      console.log(post);
      if (post.id === id){
        this.postagem.id = post.id;
        this.postagem.titulo = post.titulo;
        this.postagem.subtitulo = post.subtitulo;
        this.postagem.conteudo = post.conteudo;
        this.postagem.curtida = post.curtida;
        this.postagem.autor = post.autor;
        this.postagem.date = post.date;
        this.postagem.url = post.url;
        this.postagem.palavrasChave = post.palavrasChave;
        this.postagem.arquivoPublicacao = post.arquivoPublicacao;
        this.postagem.tipoPostagem = post.tipoPostagem;
      }
     }

    if (act === 'like'){

       this.postagem.idsCurtida.push(this.usuario.id);
       this.postagem.curtida++;
       this.updatePost();
    }
  }

  public updatePost(){
    this.postService.updatePost(this.postagem).subscribe(res => {
      this.postagens = JSON.parse(JSON.stringify(res));
      this.ngOnInit(); //Reload sem precisar recarregar a pagina
    });
  }

  public mandaPdf(base64){
    this.postService.mandaPdf(base64);
  }

  ngOnInit(): void {
    this.buildFeedUsuarioLogado();

    this.usuario = this.usuarioLogadoService.getUsuarioLogado().subscribe(data => {
    this.usuario = JSON.parse(JSON.stringify(data));
    });
  }
}
