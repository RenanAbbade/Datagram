import { Component, OnInit } from '@angular/core';
import { CadastroServiceService } from '../services/cadastro-service.service';

@Component({
  selector: 'app-cadastro',
  templateUrl: './cadastro.component.html',
  styleUrls: ['./cadastro.component.css']
})
export class CadastroComponent implements OnInit {

  tipoUsuario;

  constructor(private cadastroService: CadastroServiceService){}

  membro = {nome: '', senha: '', email: '', dataNasc: '', cpf: '', escolaridade: '', tipoUsuario: ''};

  // tslint:disable-next-line: max-line-length
  pesquisador = {nome: '', senha: '', email: '', dataNasc: '', estado: '',  municipio: '', instituicao: '', linkCv: '', dataInicio: '', escolaridade: '', tipoUsuario: ''};


  public mudaTipo(tipo){
    if (tipo === 'Pesquisador'){
      this.tipoUsuario = 'Pesquisador';
      // Modificando aparencia do selecionado
      document.getElementById('Pesquisador').className = 'btn btn-primary';
      document.getElementById('Membro').className = 'btn btn-secondary';

      // visibilizando elementos do pesquisador
      document.getElementById('pesquisadorNome').style.display = 'inline';
      document.getElementById('pesquisadorSenha').style.display = 'inline';
      document.getElementById('pesquisadorEmail').style.display = 'inline';
      document.getElementById('pesquisadorNasc').style.display = 'inline';
      document.getElementById('pesquisadorEscolaridade').style.display = 'inline';
      document.getElementById('PesquisadorEstado').style.display = 'inline';
      document.getElementById('PesquisadorMunicipio').style.display = 'inline';
      document.getElementById('PesquisadorInstituicao').style.display = 'inline';
      document.getElementById('PesquisadorLink').style.display = 'inline';
      document.getElementById('PesquisadorDataInic').style.display = 'inline';

      //escondendo elementos de membro
      document.getElementById('membroNome').style.display = 'none';
      document.getElementById('membroSenha').style.display = 'none';
      document.getElementById('membroEmail').style.display = 'none';
      document.getElementById('membroNasc').style.display = 'none';
      document.getElementById('membroEscolaridade').style.display = 'none';
      document.getElementById('membroCPF').style.display = 'none';
      document.getElementById('botaoMembro').style.display = 'none';

      //setando button
      document.getElementById('botaoPesquisador').style.display = 'block';
      document.getElementById('botaoPesquisador').style.textAlign = 'center';


    }else{
      this.tipoUsuario = 'Membro';
      // Modificando aparencia do selecionado
      document.getElementById('Membro').className = 'btn btn-primary';
      document.getElementById('Pesquisador').className = 'btn btn-secondary';

      //visualizando elementos do membro
      document.getElementById('membroNome').style.display = 'inline';
      document.getElementById('membroSenha').style.display = 'inline';
      document.getElementById('membroEmail').style.display = 'inline';
      document.getElementById('membroNasc').style.display = 'inline';
      document.getElementById('membroEscolaridade').style.display = 'inline';
      document.getElementById('membroCPF').style.display = 'inline';

      //escondendo elementos do pesquisador
      document.getElementById('pesquisadorNome').style.display = 'none';
      document.getElementById('pesquisadorSenha').style.display = 'none';
      document.getElementById('pesquisadorEmail').style.display = 'none';
      document.getElementById('pesquisadorNasc').style.display = 'none';
      document.getElementById('pesquisadorEscolaridade').style.display = 'none';
      document.getElementById('PesquisadorEstado').style.display = 'none';
      document.getElementById('PesquisadorMunicipio').style.display = 'none';
      document.getElementById('PesquisadorInstituicao').style.display = 'none';
      document.getElementById('PesquisadorLink').style.display = 'none';
      document.getElementById('PesquisadorDataInic').style.display = 'none';
      document.getElementById('botaoPesquisador').style.display = 'none';

      // setando button
      document.getElementById('botaoMembro').style.display = 'block';
      document.getElementById('botaoMembro').style.textAlign = 'center';

    }
  }

  public efetuaCadastro() {
    if (this.tipoUsuario === 'Pesquisador'){
      this.pesquisador.tipoUsuario = 'Pesquisador';
      this.cadastroService.cadastro(this.pesquisador);
    }else{
      this.membro.tipoUsuario = 'Membro';
      this.cadastroService.cadastro(this.membro);
    }
  }

  ngOnInit(): void {

  }

}
