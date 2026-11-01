import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
@Component({
  imports: [RouterLink],
  template:
    '<main class="auth card"><h1>Página não encontrada</h1><a routerLink="/feed">Voltar ao início</a></main>',
})
export class NotFoundComponent {}
