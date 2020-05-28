import { AppConstants } from './../app-constants';
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {Router} from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class LoginServiceService {

  constructor(private http: HttpClient, private router: Router) { }

  public login(usuario){

    console.log(JSON.stringify(usuario));


    return this.http.post(AppConstants.baseLogin, usuario, {responseType: 'text'}).subscribe(data => {
     console.log(data);
    // Implementar auth jwt spring security
     if (data === 'auth'){
      this.router.navigate(['home']);
     }else{
       alert('Credenciais não encontradas!');
     }
    });
    }
  }

