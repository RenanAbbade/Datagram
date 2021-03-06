import { CadastroComponent } from './cadastro/cadastro.component';
export class AppConstants {

  // Método que retorna o local da minha api spring - "/post/insert

  public static get baseServidor(): string { return 'http://localhost:8080/'; }

  public static get baseUsuarios(): string { return this.baseServidor + 'user/'; }

  public static get baseLoginInsert(): string {return this.baseUsuarios + 'insert/'; }

  public static get baseLogin(): string {return this.baseUsuarios + 'login/'; }

  // MUNICIPIO
  public static get consultaMunicipio(): string {return this.baseServidor + 'municipios/'; }

  // URL PARA VERIFICAR USUARIO LOGADO
  public static get consultaUserLogado(): string {return this.baseUsuarios + 'userLogado/'; }

  public static get baseUsuarioByNome(): string {return this.baseUsuarios + 'nome/?nome_value='; }

  public static get baseUsuarioByInstituicao(): string {return this.baseUsuarios + 'instituicao/?instituicao_value='; }

  // POST

  public static get basePostagem(): string {return this.baseServidor + 'post/'; }

  public static get postsPorUsuario(): string {return this.basePostagem + 'PostsPorUsuario/'; }

  public static get postPublicacao(): string {return this.basePostagem + 'insert/'; }

  public static get getNotificacao(): string {return this.baseServidor + 'notificacao/'; }

  //GERENTE

  public static get baseGerente(): string {return this.baseServidor + 'gerente/'; }

  public static get get10maisConectados(): string {return this.baseGerente + 'maisConectados/'; }

  public static get getNumMedioSeguidores(): string {return this.baseGerente + 'mediaSeguidores/'; }





}
