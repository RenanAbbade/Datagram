import { AppConstants } from './../app-constants';
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {Router} from '@angular/router';

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
  }

