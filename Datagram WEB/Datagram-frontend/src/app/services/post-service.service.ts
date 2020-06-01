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
/*
    updatePost(postagem): Observable<any>{
      return this.http.put<any>(AppConstants.basePostagem, postagem);

    }*/

    updatePost(postagem){
      return this.http.put(AppConstants.basePostagem, postagem);
    }

    deletePost(id){
      console.log(id);

      return this.http.delete(AppConstants.basePostagem + id);
  }
  }



/*
public getPostsUsuarioLogado(id){
    const params = new HttpParams().set('id', id); //Create new HttpParams

    return this.http.get(AppConstants.basePostagem, {params} );
  }
*/
