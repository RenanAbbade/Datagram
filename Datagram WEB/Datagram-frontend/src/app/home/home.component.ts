import { UsuarioLogadoService } from './../services/usuarioLogado.service';
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

  // tslint:disable-next-line: max-line-length
  constructor(private postService: PostServiceService, private usuarioService: UsuarioServiceService,  private UsuarioLogadoService: UsuarioLogadoService, private router: Router) { }

  postagem = {titulo: '', subtitulo: '', conteudo: '', date: '', url: '', arquivoPublicacao: ''};
  usuario = {nome: ''};
  rotaPesquisa = 'perfil-amigo';

  listaRetornoPesquisa;

  notificacoes;

  usuarioLogado;

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
  });
}

checkPage(){
  const rota = this.router.url;
  if (rota.startsWith('/feed/perfil-amigo') || rota.startsWith('/perfil/perfil-amigo')){
    this.router.navigate(['../feed']);
  }
}

getNotificacao(){
  this.postService.getNotificacoes().subscribe(res => {
    this.notificacoes = JSON.parse(JSON.stringify(res));
  });
}
public clearSessionStorage(){
  sessionStorage.clear();
}

getUsuarioLogado(){
  this.UsuarioLogadoService.getUsuarioLogado().subscribe(res => {
    this.usuarioLogado = JSON.parse(JSON.stringify(res));
    console.log(this.usuarioLogado);
  });
}

ngOnInit(): void {}

public mudaTipo(){



  if (this.usuarioLogado.tipoUsuario === 'Pesquisador'){

    // Modificando aparencia do selecionado
    document.getElementById('Cientifica').className = 'btn btn-primary';
    document.getElementById('Simples').className = 'btn btn-secondary';

    // visibilizando elementos do pesquisador
    document.getElementById('pesquisadorNome').style.display = 'inline';


    //escondendo elementos de membro
    document.getElementById('membroNome').style.display = 'none';

    //setando button
    document.getElementById('botaoPesquisador').style.display = 'block';
    document.getElementById('botaoPesquisador').style.textAlign = 'center';


  }else{
    // Modificando aparencia do selecionado
    document.getElementById('Membro').className = 'btn btn-primary';
    document.getElementById('Pesquisador').className = 'btn btn-secondary';

    //visualizando elementos do membro
    document.getElementById('membroNome').style.display = 'inline';

    //escondendo elementos do pesquisador
    document.getElementById('pesquisadorNome').style.display = 'none';

    // setando button
    document.getElementById('botaoMembro').style.display = 'block';
    document.getElementById('botaoMembro').style.textAlign = 'center';

    }
  }

  uploadArquivo(e){
    var file = e.dataTransfer ? e.dataTransfer.files[0] : e.target.files[0];
    var reader = new FileReader();

    if (!file.type.match('pdf')) {
      alert('formato inválido, por favor, insira um documento .pdf');
      return;
    }

    reader.onload = this._handleReaderLoaded.bind(this);
    reader.readAsDataURL(file);
  }
  _handleReaderLoaded(e) {
    let reader = e.target;
    this.postagem.arquivoPublicacao  = reader.result;
  }
}
