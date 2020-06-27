import { UsuarioServiceService } from './../services/usuario-service.service';
import { PerfilComponent } from './../perfil/perfil.component';
import { PostServiceService } from './../services/post-service.service';
import { CadastroComponent } from './../cadastro/cadastro.component';
import { Component, OnInit } from '@angular/core';
import { NodeWithI18n } from '@angular/compiler';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  constructor(private postService: PostServiceService, private usuarioService: UsuarioServiceService, private router: Router) { }

  postagem = {titulo: '', subtitulo: '', conteudo: '', date: ''};
  usuario = {nome: ''};
  rotaPesquisa = 'perfil-amigo';

  listaRetornoPesquisa;

  public publicarPost(){

    const dataAtual = new Date();
    const dia = dataAtual.getDate();
    const mes = dataAtual.getMonth() + 1;
    const ano = dataAtual.getFullYear();
    const hora = dataAtual.getHours();
    const minuto = dataAtual.getMinutes();


    this.postagem.date = dia + '/' + mes + '/' + ano + ' ' + hora + ':' + minuto;
    console.log(this.postagem);

    this.postService.postPublicacao(this.postagem);

    window.location.reload();

  }

public getUsuarioByNome(nome){
  this.usuarioService.getUsuarioByNome(nome).subscribe(res => {
    this.listaRetornoPesquisa = JSON.parse(JSON.stringify(res));
    console.log(this.listaRetornoPesquisa);
  });
}

checkPage(){
  const rota = this.router.url;
  if (rota.startsWith('/feed/perfil-amigo') || rota.startsWith('/perfil/perfil-amigo')){
    this.router.navigate(['../feed']);
  }
}

public clearSessionStorage(){
  sessionStorage.clear();
}

ngOnInit(): void {}

}
