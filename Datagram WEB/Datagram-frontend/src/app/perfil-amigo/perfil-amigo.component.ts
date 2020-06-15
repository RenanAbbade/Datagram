import { ActivatedRoute } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { UsuarioServiceService } from '../services/usuario-service.service';
import { PostServiceService } from '../services/post-service.service';
import { UsuarioLogadoService } from '../services/usuarioLogado.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-perfil-amigo',
  templateUrl: './perfil-amigo.component.html',
  styleUrls: ['./perfil-amigo.component.css']
})
export class PerfilAmigoComponent implements OnInit {


  usuario;

  id: string;

  Inscricao: Subscription

  postagens;

  postagem;

  interessesEscolhidos = '';

  // tslint:disable-next-line: max-line-length
  constructor(private usuarioService: UsuarioServiceService, private usuarioLogadoService: UsuarioLogadoService, private postService: PostServiceService, private route: ActivatedRoute) {
    //this.id = this.route.snapshot.params['id']; // No JS os objetos são chave valor - Aqui estou pegando o id que foi passado na URL
   }

  public updateUsuario(){
    this.usuario.seguidores++;
    this.usuarioService.updateUsuario(this.usuario).subscribe(res => {
      this.usuario = JSON.parse(JSON.stringify(res));
      this.ngOnInit();
    });

  }

  public editPost(id, act){
    for (let post of this.postagens) {
      if (post.id === id){
        this.postagem.id = post.id;
        this.postagem.titulo = post.titulo;
        this.postagem.subtitulo = post.subtitulo;
        this.postagem.conteudo = post.conteudo;
        this.postagem.curtida = post.curtida;
        this.postagem.autor = post.autor;
        this.postagem.date = post.date;
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

  ngOnInit(): void {

    this.Inscricao =  this.route.params.subscribe(
      (params: any) => {
        this.id = params['id'];
      });


    this.usuarioService.getUsuarioById(this.id).subscribe(res => {
      this.usuario = JSON.parse(JSON.stringify(res));
      console.log(this.usuario);
    });

  }

  ngOnDestroy(){
    this.Inscricao.unsubscribe();
  }

}
