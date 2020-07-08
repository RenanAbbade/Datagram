import { PerfilComponent } from './../perfil/perfil.component';
import { AppConstants } from './../app-constants';
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {Router} from '@angular/router';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PostServiceService {

  constructor(private http: HttpClient, private router: Router) { }

  public postPublicacao(postagem){

    console.log(JSON.stringify(postagem));

    return this.http.post(AppConstants.postPublicacao, postagem, {responseType: 'text'}).subscribe(data => {
     console.log(data);

    });
    }

    updatePost(postagem){
      return this.http.put(AppConstants.basePostagem, postagem);
    }

    deletePost(id){
      console.log(id);

      return this.http.delete(AppConstants.basePostagem + id);
    }

    buildFeed(){
      return this.http.get(AppConstants.basePostagem + 'feed');
    }

    getNotificacoes(){
      return this.http.get(AppConstants.getNotificacao);
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


  }



/*
public getPostsUsuarioLogado(id){
    const params = new HttpParams().set('id', id); //Create new HttpParams

    return this.http.get(AppConstants.basePostagem, {params} );
  }
*/
