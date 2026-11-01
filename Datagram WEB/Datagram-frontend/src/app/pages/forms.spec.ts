import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  provideHttpClientTesting,
  HttpTestingController,
} from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { LoginComponent } from './login.component';
import { RegisterComponent } from './register.component';
import { PostEditorComponent } from '../shared/post-editor.component';
import { sampleUser } from '../core/test-fixtures';

describe('Reactive forms in zoneless components', () => {
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LoginComponent, RegisterComponent, PostEditorComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('enables login after valid input without manual page refresh', async () => {
    const f = TestBed.createComponent(LoginComponent);
    f.detectChanges();
    expect(f.nativeElement.querySelector('button').disabled).toBe(true);
    for (const [id, value] of [
      ['email', 'test@example.com'],
      ['senha', 'Password123!'],
    ]) {
      const input = f.nativeElement.querySelector('#' + id);
      input.value = value;
      input.dispatchEvent(new Event('input'));
    }
    await f.whenStable();
    expect(f.nativeElement.querySelector('button').disabled).toBe(false);
  });
  it('validates the researcher form after switching from member', async () => {
    const f = TestBed.createComponent(RegisterComponent);
    f.detectChanges();
    f.componentInstance.form.patchValue({
      nome: 'Researcher',
      email: 'test@example.com',
      senha: 'Password123!',
      dataNasc: '1990-01-01',
      tipoUsuario: 'Pesquisador',
      instituicao: 'Test',
      dataInicio: '2015-01-01',
    });
    f.detectChanges();
    await f.whenStable();
    expect(f.componentInstance.form.valid).toBe(true);
    expect(f.nativeElement.querySelector('button').disabled).toBe(false);
  });
  it('allows retrying a failed publication without losing form contents', () => {
    const f = TestBed.createComponent(PostEditorComponent);
    f.detectChanges();
    const c = f.componentInstance;
    c.form.patchValue({ titulo: 'Title', conteudo: 'Body' });
    c.save();
    http
      .expectOne('/api/post/insert')
      .flush({ message: 'Failure' }, { status: 500, statusText: 'Error' });
    expect(c.busy()).toBe(false);
    expect(c.form.controls.titulo.value).toBe('Title');
    c.save();
    http.expectOne('/api/post/insert').flush({ id: 1, autor: sampleUser });
    expect(c.error()).toBe('');
  });
});
