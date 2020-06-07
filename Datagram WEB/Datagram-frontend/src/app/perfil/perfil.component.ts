import { PostServiceService } from './../services/post-service.service';
import { UsuarioLogadoService } from './../services/usuarioLogado.service';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UsuarioServiceService } from '../services/usuario-service.service';
import { HttpClient } from '@angular/common/http';
import { AppConstants } from '../app-constants';


@Component({
  selector: 'app-perfil',
  templateUrl: './perfil.component.html',
  styleUrls: ['./perfil.component.css']
})
export class PerfilComponent implements OnInit {

  usuario;

  id;

  postagens;

  postagem = {id: Number, autor: '', titulo: '', subtitulo: '', conteudo: '', date: '', curtida: 0, idsCurtida: []};

  UFS: Array<string> = ['AC', 'AL', 'AM', 'AP', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MG', 'MS', 'MT', 'PA', 'PB', 'PE', 'PI', 'PR', 'RJ', 'RN', 'RO', 'RR', 'RS', 'SC', 'SE', 'SP', 'TO'];

  Municipios: Array<string> = [];

  // tslint:disable-next-line: max-line-length
  constructor(private usuarioService: UsuarioServiceService, private usuarioLogadoService: UsuarioLogadoService, private postService: PostServiceService, private route: ActivatedRoute, private router: Router, private http: HttpClient) {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
   }



  public updateUsuario(){

    this.usuarioService.updateUsuario(this.usuario).subscribe(res => {
      this.usuario = JSON.parse(JSON.stringify(res));
      this.ngOnInit();
    });

  }

  public initMunicipio(){

    if (this.usuario.estado != null){
      this.http.post(AppConstants.consultaMunicipio, this.usuario.estado).subscribe(data => {

          this.Municipios = JSON.parse(JSON.stringify(data));
      });
    }
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
    if (act === 'del'){
       this.deletePost();
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
      this.ngOnInit();//Reload sem precisar recarregar a pagina
    });
  }

  public deletePost(){
    this.postService.deletePost(this.postagem.id).subscribe(res => {
      this.postagens = JSON.parse(JSON.stringify(res));
      this.ngOnInit();
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

