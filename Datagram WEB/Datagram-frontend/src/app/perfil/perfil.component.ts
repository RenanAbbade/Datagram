import { PostServiceService } from './../services/post-service.service';
import { UsuarioLogadoService } from './../services/usuarioLogado.service';
import { Component, OnInit } from '@angular/core';


@Component({
  selector: 'app-perfil',
  templateUrl: './perfil.component.html',
  styleUrls: ['./perfil.component.css']
})
export class PerfilComponent implements OnInit {

  usuario;

  id;

  postagens;


  postagem = {id: Number, titulo: '', subtitulo: '', conteudo: '', date: ''};

  constructor(private usuarioLogadoService: UsuarioLogadoService, private postService: PostServiceService) { }

  public editPost(id, act){
    for (let post of this.postagens) {
      if (post.id === id){
        this.postagem.id = post.id;
        this.postagem.titulo = post.titulo;
        this.postagem.subtitulo = post.subtitulo;
        this.postagem.conteudo = post.conteudo;
      }
     }
    if (act === 'del'){
       this.deletePost();
     }
  }


  public updatePost(){
    this.postService.updatePost(this.postagem).subscribe(res => {
      this.postagens = JSON.parse(JSON.stringify(res));
    });
  }

  public deletePost(){
    this.postService.deletePost(this.postagem.id).subscribe(res => {
      this.postagens = JSON.parse(JSON.stringify(res));
    });
  }


  ngOnInit(): void {

    this.usuario = this.usuarioLogadoService.getUsuarioLogado().subscribe(data => {
    this.usuario = JSON.parse(JSON.stringify(data));

    this.usuarioLogadoService.getPostsUsuarioLogado(this.usuario.id).subscribe(res => {
    this.postagens = JSON.parse(JSON.stringify(res));
    console.log(this.postagens[0]);
      });
    });
  }
}
