import { AppConstants } from '../app-constants';
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {Router} from '@angular/router';

@Injectable({
  providedIn: 'root'
})
// tslint:disable-next-line: align
export class GerenteServiceService {

  constructor(private http: HttpClient, private router: Router) { }

  public getNumMediaSeguidores(){
    return this.http.get(AppConstants.getNumMedioSeguidores);
  }

  public get10maisConectados(){
    return this.http.get(AppConstants.get10maisConectados);
  }
}
