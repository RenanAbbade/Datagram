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

  // tslint:disable-next-line: max-line-length
  postagem = {id: Number, autor: '', titulo: '', subtitulo: '', conteudo: '', date: '', curtida: 0, idsCurtida: [], url: '', arquivoPublicacao: '', palavrasChave:[], tipoPostagem: ''};

  interessesEscolhidos = '';

  seguidor;

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

  verificaSeguidor(){
    this.usuarioService.getFollower(this.usuario.id).subscribe(res => {
      console.log(res);
      this.seguidor = res;
      if (res === 'true') {
        document.getElementById('follow').className = 'btn btn-success';
        document.getElementById('follow').innerText = 'seguindo!';

      }else{
        document.getElementById('follow').className = 'btn btn-primary';
        document.getElementById('follow').innerText = 'seguir';
      }

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
      this.verificaSeguidor();
    });

    this.usuarioService.getPostsUsuario(this.id).subscribe(res => {
      this.postagens = JSON.parse(JSON.stringify(res));
      console.log(this.postagens);
  });


  }

  public mandaPdf(data64){
    var type = 'application/pdf';
    let blob = null;
    const blobURL = URL.createObjectURL( this.pdfBlobConversion(data64, 'application/pdf'));
    const theWindow = window.open(blobURL);
    const theDoc = theWindow.document;
    const theScript = document.createElement('script');

    function injectThis() {
            window.print();
    }

    theScript.innerHTML = 'window.onload = ${injectThis.toString()};';
    theDoc.body.appendChild(theScript);

  }

  //converts base64 to blob type for windows
  public pdfBlobConversion(b64Data, contentType) {
    contentType = contentType || '';
    var sliceSize = 512;
    b64Data = b64Data.replace(/^[^,]+,/, '');
    b64Data = b64Data.replace(/\s/g, '');
    var byteCharacters = window.atob(b64Data);
    var byteArrays = [];

    for ( var offset = 0; offset < byteCharacters.length; offset = offset + sliceSize ) {
      var slice = byteCharacters.slice(offset, offset + sliceSize);

      var byteNumbers = new Array(slice.length);
      for (var i = 0; i < slice.length; i++) {
        byteNumbers[i] = slice.charCodeAt(i);
      }

      var byteArray = new Uint8Array(byteNumbers);

      byteArrays.push(byteArray);
    }

    var blob = new Blob(byteArrays, { type: contentType });
    return blob;
  }

  ngOnDestroy(){
    this.Inscricao.unsubscribe();
  }

}
