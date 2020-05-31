import { UsuarioLogadoService } from './../services/usuarioLogado.service';
import { Component, OnInit } from '@angular/core';


@Component({
  selector: 'app-perfil',
  templateUrl: './perfil.component.html',
  styleUrls: ['./perfil.component.css']
})
export class PerfilComponent implements OnInit {

  usuario;

  constructor(private usuarioLogadoService: UsuarioLogadoService) { }


  ngOnInit(): void {
    this.usuario = this.usuarioLogadoService.getUsuarioLogado().subscribe(data => {
      this.usuario = JSON.parse(JSON.stringify(data));
      console.log(this.usuario);
       });

  }
}

