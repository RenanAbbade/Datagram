import { Component, inject, signal } from '@angular/core';
import { ApiService, errorMessage } from '../core/api.service';
import { Post } from '../core/models';
import { PostCardComponent } from '../shared/post-card.component';
@Component({
  imports: [PostCardComponent],
  template: `<h1>Notificações</h1>
    @if (error()) {
      <p role="alert" class="error">{{ error() }}</p>
    }
    @for (post of posts(); track post.id) {
      <app-post-card [post]="post" (updated)="update($event)" />
    } @empty {
      <p>Nenhuma notificação.</p>
    }`,
})
export class NotificationsComponent {
  private api = inject(ApiService);
  posts = signal<Post[]>([]);
  error = signal('');
  ngOnInit() {
    this.api.notifications().subscribe({
      next: (p) => this.posts.set(p),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
  update(post: Post) {
    this.posts.update((p) => p.map((x) => (x.id === post.id ? post : x)));
  }
}
