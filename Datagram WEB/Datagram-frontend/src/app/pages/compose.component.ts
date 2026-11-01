import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { SessionService } from '../core/session.service';
import { PostEditorComponent } from '../shared/post-editor.component';
@Component({
  imports: [PostEditorComponent],
  template:
    '<section class="card stack"><h1>Nova publicação</h1><app-post-editor (saved)="saved()"/></section>',
})
export class ComposeComponent {
  private router = inject(Router);
  private session = inject(SessionService);
  saved() {
    this.session.refresh().subscribe();
    void this.router.navigateByUrl('/feed');
  }
}
