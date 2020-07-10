import { GerenteServiceService } from './../services/gerente-service.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-gerente',
  templateUrl: './gerente.component.html',
  styleUrls: ['./gerente.component.css']
})
export class GerenteComponent implements OnInit {

  numMedioSeguidores;

  usuariosMaisConectados;

  constructor(private service: GerenteServiceService) { }

  public getMediaSeguidores(){
    this.service.getNumMediaSeguidores().subscribe(res => {
      this.numMedioSeguidores = JSON.parse(JSON.stringify(res));
      console.log(JSON.stringify(this.numMedioSeguidores));
    });
  }

  public getMaisConectados(){
    this.service.get10maisConectados().subscribe(res => {
      this.usuariosMaisConectados = JSON.parse(JSON.stringify(res));
      console.log(JSON.stringify(this.usuariosMaisConectados));
    });
  }

  ngOnInit(): void {
    this.getMaisConectados();
    this.getMediaSeguidores();
  }
}
