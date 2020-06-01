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

  id: number;

  postagens;

  postEdit = {titulo: '', subtitulo: '', conteudo: ''};

  constructor(private usuarioLogadoService: UsuarioLogadoService, private postService: PostServiceService) { }

  public editPost(id){
    for (let post of this.postagens) {
      if (post.id === id){
        this.id = post.id;
        this.postEdit.titulo = post.titulo;
        this.postEdit.subtitulo = post.subtitulo;
        this.postEdit.conteudo = post.conteudo;
      }
     }
  }


  public updatePost(){
    this.postService.updatePublicacao(this.id, this.postEdit).subscribe(res => {
      this.postagens = JSON.parse(JSON.stringify(res));
      console.log(this.postagens[0]);
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
