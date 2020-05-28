import { CadastroComponent } from './../cadastro/cadastro.component';
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


      if (data === 'CREATED'){
      alert('Usuário cadastrado com sucesso!');
      this.router.navigate(['home']);
     }
     else if (data === 'EMAIL') {
      alert('Já existe um usuário cadastrado com este email');
     }
     else if (data === 'CPF') {
      alert('CPF inválido, por favor, tente novamente.');
     }
     else if (data === 'DATE') {
      alert('Datas inválidas! Necessário que o usuário tenha mais de 18 anos.');
     }
     else {
      alert('Erro do servidor, contate o administrador do sistema.');
     }


    });
    }
  }
