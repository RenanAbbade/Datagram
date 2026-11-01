import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, finalize, forkJoin, of, switchMap, tap } from 'rxjs';
import { ApiService, errorMessage } from '../core/api.service';
import { SessionService } from '../core/session.service';
import { Post, User } from '../core/models';
import { AvatarComponent } from '../shared/avatar.component';
import { PostCardComponent } from '../shared/post-card.component';
import { FileService } from '../shared/file.service';
import { ProfileFormComponent } from './profile-form.component';
@Component({
  imports: [
    AvatarComponent,
    PostCardComponent,
    ProfileFormComponent,
    RouterLink,
  ],
  template: ` @if (error()) {
      <p role="alert" class="error">{{ error() }}</p>
    }
    @if (user(); as u) {
      <section class="card stack">
        <div class="row">
          <app-avatar [user]="displayedUser()" />
          <h1>{{ displayedUser()?.nome }}</h1>
        </div>
        <p>
          {{ u.tipoUsuario }} · {{ u.instituicao || 'Sem instituição' }} ·
          {{ u.municipio }} {{ u.estado }}
        </p>
        <p>
          {{ displayedUser()?.seguidores }} seguidores ·
          {{ displayedUser()?.seguindo }} seguindo ·
          {{ displayedUser()?.posts }} publicações
        </p>
        @if (u.linkCv) {
          <a [href]="u.linkCv" target="_blank" rel="noopener noreferrer"
            >Currículo</a
          >
        }
        <div class="row wrap">
          @for (tag of u.interesses; track tag) {
            <span class="tag">{{ tag }}</span>
          }
        </div>
        @if (own()) {
          <label
            >Alterar foto (PNG, JPEG ou WebP, até 2 MB)<input
              type="file"
              accept="image/png,image/jpeg,image/webp"
              (change)="photo($event)"
              [disabled]="busy()"
          /></label>
          <div class="row wrap">
            <button
              type="button"
              class="secondary"
              (click)="editing.set(!editing())"
            >
              {{ editing() ? 'Fechar edição' : 'Editar perfil' }}</button
            ><button
              type="button"
              class="secondary"
              (click)="people('seguidores')"
            >
              Seguidores</button
            ><button
              type="button"
              class="secondary"
              (click)="people('seguindo')"
            >
              Seguindo</button
            ><button
              type="button"
              class="secondary"
              (click)="people('interesses')"
            >
              Sugestões</button
            ><button type="button" class="secondary" (click)="popular()">
              Mais curtidas
            </button>
          </div>
        } @else {
          <button type="button" [disabled]="busy()" (click)="follow()">
            {{ following() ? 'Deixar de seguir' : 'Seguir' }}
          </button>
        }
        @if (busy()) {
          <p role="status">Salvando…</p>
        }
      </section>
      @if (editing() && own()) {
        <app-profile-form
          [user]="displayedUser()!"
          (saved)="user.set($event); editing.set(false)"
        />
      }
      @if (peopleTitle()) {
        <section class="card stack">
          <h2>{{ peopleTitle() }}</h2>
          @for (person of peopleList(); track person.id) {
            <a [routerLink]="['/usuarios', person.id]" class="row"
              ><app-avatar [user]="person" />{{ person.nome }}</a
            >
          } @empty {
            <p>Nenhum usuário encontrado.</p>
          }
        </section>
      }
      <h2>Publicações</h2>
      @for (post of posts(); track post.id) {
        <app-post-card
          [post]="post"
          (updated)="update($event)"
          (deleted)="remove($event)"
        />
      }
      @if (!loading() && !posts().length) {
        <p class="card">Nenhuma publicação.</p>
      }
      @if (hasNext() && !loading()) {
        <button type="button" (click)="more()">Carregar mais</button>
      }
    }
    @if (loading()) {
      <p role="status">Carregando…</p>
    }`,
})
export class ProfileComponent {
  private api = inject(ApiService);
  session = inject(SessionService);
  private route = inject(ActivatedRoute);
  private destroy = inject(DestroyRef);
  private files = inject(FileService);
  user = signal<User | null>(null);
  own = computed(() => this.user()?.id === this.session.user()?.id);
  displayedUser = computed(() =>
    this.own() ? this.session.user() : this.user(),
  );
  posts = signal<Post[]>([]);
  following = signal(false);
  loading = signal(false);
  busy = signal(false);
  error = signal('');
  editing = signal(false);
  hasNext = signal(false);
  peopleList = signal<User[]>([]);
  peopleTitle = signal('');
  page = 1;
  ngOnInit() {
    this.route.paramMap
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.error.set('');
          this.editing.set(false);
          this.peopleTitle.set('');
          this.user.set(null);
          this.posts.set([]);
        }),
        switchMap((params) => {
          const id = Number(params.get('id') || this.session.user()?.id);
          return forkJoin({
            user: this.api.user(id),
            posts: this.api.posts(0, id),
            following:
              id === this.session.user()?.id
                ? of(false)
                : this.api.following(id),
          }).pipe(
            catchError((e) => {
              this.error.set(errorMessage(e));
              this.loading.set(false);
              return of(null);
            }),
          );
        }),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe((result) => {
        this.loading.set(false);
        if (result) {
          this.user.set(result.user);
          this.posts.set(result.posts.content);
          this.hasNext.set(result.posts.hasNext);
          this.following.set(result.following);
          this.page = 1;
        }
      });
  }
  photo(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file || this.busy()) return;
    this.error.set('');
    try {
      this.files.validate(file, 'image');
    } catch (e) {
      this.error.set(errorMessage(e));
      return;
    }
    this.busy.set(true);
    this.api
      .photo(file)
      .pipe(
        finalize(() => this.busy.set(false)),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe({
        next: (u) => {
          this.session.setUser(u);
          if (this.user()?.id === u.id) this.user.set(u);
        },
        error: (e) => this.error.set(errorMessage(e)),
      });
  }
  follow() {
    const u = this.user();
    if (!u || this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    this.api
      .follow(u.id, !this.following())
      .pipe(
        finalize(() => this.busy.set(false)),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe({
        next: (u) => {
          if (this.user()?.id === u.id) {
            this.user.set(u);
            this.following.update((v) => !v);
          }
          this.session
            .refresh()
            .subscribe({ error: (e) => this.error.set(errorMessage(e)) });
        },
        error: (e) => this.error.set(errorMessage(e)),
      });
  }
  more() {
    const id = this.user()?.id;
    if (id === undefined || this.loading()) return;
    this.loading.set(true);
    this.api
      .posts(this.page, id)
      .pipe(
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe({
        next: (r) => {
          if (this.user()?.id !== id) return;
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
  people(kind: 'seguidores' | 'seguindo' | 'interesses') {
    this.error.set('');
    this.api
      .people(kind)
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({
        next: (p) => {
          this.peopleList.set(p);
          this.peopleTitle.set(
            kind === 'interesses'
              ? 'Sugestões'
              : kind === 'seguidores'
                ? 'Seguidores'
                : 'Seguindo',
          );
        },
        error: (e) => this.error.set(errorMessage(e)),
      });
  }
  popular() {
    this.api
      .popular()
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({
        next: (p) => {
          this.posts.set(p);
          this.hasNext.set(false);
        },
        error: (e) => this.error.set(errorMessage(e)),
      });
  }
}
