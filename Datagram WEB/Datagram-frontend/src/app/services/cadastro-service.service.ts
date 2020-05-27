import { AppConstants } from './../app-constants';
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {Router} from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class CadastroServiceService {

  constructor(private http: HttpClient, private router: Router) { }

  public cadastro(usuario){

    console.log(JSON.stringify(usuario));


    return this.http.post(AppConstants.baseLoginInsert, usuario, {responseType: 'text'}).subscribe(data => {
     console.log(data);

     if (data === 'created'){
      this.router.navigate(['home']);
     }else{
       alert('Houve um erro ao tentar realizar cadastro!');
     }
    });
    }
  }
