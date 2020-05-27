export class AppConstants {

  // Método que retorna o local da minha api spring

  public static get baseServidor(): string { return 'http://localhost:8080/'; }

  public static get baseLoginInsert(): string {return this.baseServidor + 'login/insert'; }

  public static get baseLogin(): string {return this.baseServidor + 'login/?'; }

}
