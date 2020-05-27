import { Component, OnInit } from '@angular/core';
import { LoginServiceService } from '../services/login-service.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {


  usuario = {email: '', senha: ''};

  constructor(private loginService: LoginServiceService){}

  public testeLogin() {
    if (this.usuario.email && this.usuario.senha){
      this.loginService.login(this.usuario);
    }

  }

  ngOnInit(): void {
  }

}
