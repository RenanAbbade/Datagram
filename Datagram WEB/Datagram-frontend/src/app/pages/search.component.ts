import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  forkJoin,
  map,
  of,
  switchMap,
  tap,
} from 'rxjs';
import { ApiService, errorMessage } from '../core/api.service';
import { User } from '../core/models';
import { AvatarComponent } from '../shared/avatar.component';
@Component({
  imports: [ReactiveFormsModule, RouterLink, AvatarComponent],
  template: `<section class="card stack">
    <h1>Encontrar pessoas</h1>
    <label
      >Nome ou instituição<input
        type="search"
        [formControl]="query"
        placeholder="Digite pelo menos 2 caracteres"
    /></label>
    @if (loading()) {
      <p role="status">Buscando…</p>
    }
    @if (error()) {
      <p role="alert" class="error">{{ error() }}</p>
    }
    @for (user of users(); track user.id) {
      <a class="row" [routerLink]="['/usuarios', user.id]"
        ><app-avatar [user]="user" /><span
          >{{ user.nome }}<small>{{ user.instituicao }}</small></span
        ></a
      >
    }
    @if (searched() && !loading() && !users().length && !error()) {
      <p>Nenhum resultado.</p>
    }
  </section>`,
})
export class SearchComponent {
  private api = inject(ApiService);
  private destroy = inject(DestroyRef);
  query = new FormControl('', { nonNullable: true });
  users = signal<User[]>([]);
  loading = signal(false);
  searched = signal(false);
  error = signal('');
  constructor() {
    this.query.valueChanges
      .pipe(
        map((q) => q.trim()),
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((q) => {
          this.error.set('');
          this.searched.set(q.length >= 2);
          this.loading.set(q.length >= 2);
          if (q.length < 2) return of([] as User[]);
          return forkJoin([
            this.api.search(q, 'nome'),
            this.api.search(q, 'instituicao'),
          ]).pipe(
            map((lists) =>
              Array.from(new Map(lists.flat().map((u) => [u.id, u])).values()),
            ),
            catchError((e) => {
              this.error.set(errorMessage(e));
              return of([] as User[]);
            }),
          );
        }),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe((u) => {
        this.users.set(u);
        this.loading.set(false);
      });
  }
}
