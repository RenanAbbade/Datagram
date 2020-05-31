import { CadastroComponent } from './cadastro/cadastro.component';
export class AppConstants {

  // Método que retorna o local da minha api spring

  public static get baseServidor(): string { return 'http://localhost:8080/'; }

  public static get baseUsuarios(): string { return 'http://localhost:8080/user/'; }

  public static get baseLoginInsert(): string {return this.baseUsuarios + 'insert/'; }

  public static get baseLogin(): string {return this.baseUsuarios + 'login/'; }

  public static get consultaMunicipio(): string {return this.baseServidor + 'municipios/'; }

  public static get consultaUserLogado(): string {return this.baseUsuarios + 'userLogado/'; }





}
