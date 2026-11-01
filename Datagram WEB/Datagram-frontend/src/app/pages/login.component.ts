import { toSignal } from '@angular/core/rxjs-interop';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { SessionService } from '../core/session.service';
import { errorMessage } from '../core/api.service';
@Component({
  imports: [ReactiveFormsModule, RouterLink],
  template: `<main class="auth card stack">
    <h1>Entrar no Datagram</h1>
    <p>Uma rede para compartilhar conhecimento.</p>
    <form [formGroup]="form" (ngSubmit)="login()" class="stack">
      <label
        >Email<input
          id="email"
          type="email"
          formControlName="email"
          autocomplete="username"
          required
      /></label>
      <label
        >Senha<input
          id="senha"
          type="password"
          formControlName="senha"
          autocomplete="current-password"
          required
      /></label>
      @if (error()) {
        <p role="alert" class="error">{{ error() }}</p>
      }
      <button type="submit" [disabled]="formStatus() !== 'VALID' || busy()">
        {{ busy() ? 'Entrando…' : 'Entrar' }}
      </button>
    </form>
    <a routerLink="/cadastro">Criar conta</a>
  </main>`,
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private session = inject(SessionService);
  private router = inject(Router);
  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', Validators.required],
  });
  formStatus = toSignal(this.form.statusChanges, {
    initialValue: this.form.status,
  });
  busy = signal(false);
  error = signal('');
  login() {
    if (this.form.invalid || this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    const v = this.form.getRawValue();
    this.session
      .login(v.email, v.senha)
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: () => void this.router.navigateByUrl('/feed'),
        error: (e) => this.error.set(errorMessage(e)),
      });
  }
}
