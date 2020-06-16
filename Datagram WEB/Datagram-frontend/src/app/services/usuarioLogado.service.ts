import { CadastroComponent } from './../cadastro/cadastro.component';
import { AppConstants } from './../app-constants';
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {Router} from '@angular/router';
import { EventEmitter } from 'events';

@Injectable({
  providedIn: 'root'
})
export class UsuarioLogadoService {

  constructor(private http: HttpClient, private router: Router) { }


  public getUsuarioLogado(){
    return this.http.get(AppConstants.consultaUserLogado);
  }


}


