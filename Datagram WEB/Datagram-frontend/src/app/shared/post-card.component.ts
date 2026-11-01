import {
  Component,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';

import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Post } from '../core/models';
import { SessionService } from '../core/session.service';
import { ApiService, errorMessage } from '../core/api.service';
import { AvatarComponent } from './avatar.component';
import { PostEditorComponent } from './post-editor.component';
import { FileService } from './file.service';
@Component({
  selector: 'app-post-card',
  imports: [RouterLink, AvatarComponent, PostEditorComponent],
  template: ` <article class="card stack">
    <header class="row">
      <app-avatar [user]="author()" />
      <div>
        <a [routerLink]="['/usuarios', post().autor.id]">{{ author().nome }}</a
        ><small>{{ formattedDate() }}</small>
      </div>
    </header>
    @if (editing()) {
      <app-post-editor
        [post]="post()"
        (saved)="updated.emit($event); editing.set(false)"
      /><button type="button" class="secondary" (click)="editing.set(false)">
        Cancelar edição
      </button>
    } @else {
      <h2>{{ post().titulo }}</h2>
      <p>{{ post().subtitulo }}</p>
      <p class="content">{{ post().conteudo }}</p>
      @if (post().url) {
        <a [href]="post().url" target="_blank" rel="noopener noreferrer"
          >Abrir publicação</a
        >
      }
      <div class="row wrap">
        @for (tag of post().palavrasChave; track tag) {
          <span class="tag">{{ tag }}</span>
        }
      </div>
      <div class="row wrap">
        <button
          type="button"
          class="secondary"
          [disabled]="busy()"
          [attr.aria-pressed]="post().curtido"
          (click)="like()"
        >
          {{ post().curtido ? 'Descurtir' : 'Curtir' }} · {{ post().curtida }}
        </button>
        @if (post().temArquivo) {
          <button
            type="button"
            class="secondary"
            [disabled]="busy()"
            (click)="download()"
          >
            Baixar PDF
          </button>
        }
        @if (own()) {
          <button type="button" class="secondary" (click)="editing.set(true)">
            Editar</button
          ><button
            type="button"
            class="danger"
            [disabled]="busy()"
            (click)="remove()"
          >
            Excluir
          </button>
        }
      </div>
    }
    @if (confirmDelete()) {
      <p>Excluir esta publicação?</p>
      <div class="row">
        <button
          type="button"
          class="danger"
          (click)="remove(true)"
          [disabled]="busy()"
        >
          Confirmar exclusão</button
        ><button
          type="button"
          class="secondary"
          (click)="confirmDelete.set(false)"
        >
          Cancelar
        </button>
      </div>
    }
    @if (error()) {
      <p role="alert" class="error">{{ error() }}</p>
    }
  </article>`,
})
export class PostCardComponent {
  post = input.required<Post>();
  updated = output<Post>();
  deleted = output<number>();
  private api = inject(ApiService);
  private files = inject(FileService);
  session = inject(SessionService);
  author = computed(() =>
    this.session.user()?.id === this.post().autor.id
      ? this.session.user()!
      : this.post().autor,
  );
  formattedDate = computed(() => {
    const date = new Date(this.post().date);
    return Number.isNaN(date.getTime())
      ? this.post().date
      : new Intl.DateTimeFormat('pt-BR', {
          dateStyle: 'short',
          timeStyle: 'short',
        }).format(date);
  });
  own = computed(() => this.session.user()?.id === this.post().autor.id);
  busy = signal(false);
  editing = signal(false);
  confirmDelete = signal(false);
  error = signal('');
  like() {
    if (this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    this.api
      .like(this.post())
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: (p) => this.updated.emit(p),
        error: (e) => this.error.set(errorMessage(e)),
      });
  }
  remove(confirmed = false) {
    if (!confirmed) {
      this.confirmDelete.set(true);
      return;
    }
    this.busy.set(true);
    this.error.set('');
    this.api
      .delete(this.post().id)
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: () => {
          this.deleted.emit(this.post().id);
          this.session.refresh().subscribe();
        },
        error: (e) => this.error.set(errorMessage(e)),
      });
  }
  download() {
    this.busy.set(true);
    this.error.set('');
    this.api
      .attachment(this.post().id)
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: (b) => this.files.download(b, `publicacao-${this.post().id}.pdf`),
        error: (e) => this.error.set(errorMessage(e)),
      });
  }
}
