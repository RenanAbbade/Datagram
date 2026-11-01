import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiService, errorMessage } from '../core/api.service';
import { Post } from '../core/models';
import { PostCardComponent } from '../shared/post-card.component';
@Component({
  imports: [PostCardComponent, RouterLink],
  template: `<div class="row between">
      <h1>Seu feed</h1>
      <a routerLink="/publicar">Nova publicação</a>
    </div>
    <p>Publicações suas e de quem você segue.</p>
    @if (error()) {
      <p role="alert" class="error">{{ error() }}</p>
      <button type="button" (click)="load()">Tentar novamente</button>
    }
    @for (post of posts(); track post.id) {
      <app-post-card
        [post]="post"
        (updated)="update($event)"
        (deleted)="remove($event)"
      />
    }
    @if (!loading() && !posts().length && !error()) {
      <p class="card">
        Nada por aqui ainda. Publique ou encontre pessoas em
        <a routerLink="/buscar">Buscar</a>.
      </p>
    }
    @if (loading()) {
      <p role="status">Carregando…</p>
    }
    @if (hasNext() && !loading()) {
      <button type="button" (click)="load()">Carregar mais</button>
    }`,
})
export class FeedComponent {
  private api = inject(ApiService);
  posts = signal<Post[]>([]);
  loading = signal(false);
  error = signal('');
  hasNext = signal(false);
  page = 0;
  ngOnInit() {
    this.load();
  }
  load() {
    if (this.loading()) return;
    this.loading.set(true);
    this.error.set('');
    this.api
      .posts(this.page)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (r) => {
          this.posts.update((p) => [
            ...p,
            ...r.content.filter((n) => !p.some((x) => x.id === n.id)),
          ]);
          this.hasNext.set(r.hasNext);
          this.page++;
        },
        error: (e) => this.error.set(errorMessage(e)),
      });
  }
  update(post: Post) {
    this.posts.update((p) => p.map((x) => (x.id === post.id ? post : x)));
  }
  remove(id: number) {
    this.posts.update((p) => p.filter((x) => x.id !== id));
  }
}
