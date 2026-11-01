import { Component, computed, input } from '@angular/core';
import { User } from '../core/models';
import { environment } from '../../environments/environment';
@Component({
  selector: 'app-avatar',
  template: `<img
    [src]="source()"
    [alt]="user()?.nome || 'Usuário'"
    width="48"
    height="48"
    loading="lazy"
  />`,
  styles: [
    `
      img {
        border-radius: 50%;
        object-fit: cover;
        background: #eee;
      }
    `,
  ],
})
export class AvatarComponent {
  user = input<User | null>(null);
  source = computed(
    () =>
      this.user()?.fotoPerfil?.replace(/^\/api(?=\/)/, environment.apiUrl) ||
      '/assets/img/usuario.png',
  );
}
