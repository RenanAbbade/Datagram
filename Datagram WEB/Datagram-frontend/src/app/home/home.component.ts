import { UsuarioServiceService } from './../services/usuario-service.service';
import { PerfilComponent } from './../perfil/perfil.component';
import { PostServiceService } from './../services/post-service.service';
import { CadastroComponent } from './../cadastro/cadastro.component';
import { Component, OnInit } from '@angular/core';
import { NodeWithI18n } from '@angular/compiler';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  constructor(private postService: PostServiceService, private usuarioService: UsuarioServiceService) { }

  postagem = {titulo: '', subtitulo: '', conteudo: '', date: ''};
  usuario = {nome: ''};

  public publicarPost(){

    const dataAtual = new Date();
    const dia = dataAtual.getDate();
    const mes = dataAtual.getMonth() + 1;
    const ano = dataAtual.getFullYear();
    const hora = dataAtual.getHours();
    const minuto = dataAtual.getMinutes();


    this.postagem.date = dia + '/' + mes + '/' + ano + ' ' + hora + ':' + minuto;
    console.log(this.postagem.date);

    this.postService.postPublicacao(this.postagem);

    window.location.reload();

  }
/*

"/post/insert

 this.id = id;
        this.autor = autor;
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.conteudo = conteudo;
        this.date = date;
        this.curtida = curtida;

*/
public getUsuarioByNome(nome){
  this.usuarioService.getUsuarioByNome(nome);
}


ngOnInit(): void { }

}
