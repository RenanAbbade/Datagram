import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { SessionService } from './session.service';
import { ApiService } from './api.service';
import { authInterceptor } from './auth';
import { User } from './models';
import { sampleUser } from './test-fixtures';

describe('Session and HTTP contracts', () => {
  let http: HttpTestingController;
  let session: SessionService;
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    session = TestBed.inject(SessionService);
  });
  afterEach(() => {
    http.verify();
    sessionStorage.clear();
  });
  it('consumes JSON login and sends the token only to its API', () => {
    session.login('test@example.com', 'Password123!').subscribe();
    http
      .expectOne('/api/user/login')
      .flush({ token: 'signed', usuario: sampleUser });
    expect(session.user()?.nome).toBe('Renan');
    TestBed.inject(ApiService).posts().subscribe();
    const request = http.expectOne('/api/post/feed?page=0&size=20');
    expect(request.request.headers.get('Authorization')).toBe('Bearer signed');
    request.flush({ content: [], hasNext: false });
  });
  it('updates every consumer through a single shared state', () => {
    session.setUser(sampleUser);
    const first = session.user;
    const second = session.user;
    session.setUser({ ...sampleUser, fotoPerfil: '/api/user/1/foto?v=new' });
    expect(first()?.fotoPerfil).toBe(second()?.fotoPerfil);
    expect(first()?.fotoPerfil).toContain('new');
  });
  it('handles successful registration as HTTP 201, without legacy text sentinels', () => {
    TestBed.inject(ApiService)
      .register({
        nome: 'Renan',
        email: 'test@example.com',
        senha: 'Password123!',
        dataNasc: '1990-01-01',
        tipoUsuario: 'Membro',
        cpf: '52998224725',
        escolaridade: '',
        estado: '',
        municipio: '',
        instituicao: '',
        linkCv: '',
        dataInicio: '',
      })
      .subscribe((u) => expect(u.id).toBe(1));
    http
      .expectOne('/api/user/insert')
      .flush(sampleUser, { status: 201, statusText: 'Created' });
  });
  it('does not turn a failed profile mutation into a null user', () => {
    session.setUser(sampleUser);
    TestBed.inject(ApiService)
      .profile({
        nome: 'New',
        escolaridade: '',
        estado: '',
        municipio: '',
        instituicao: '',
        linkCv: '',
        interesses: [],
      })
      .subscribe({ error: () => {} });
    http
      .expectOne('/api/user')
      .flush({ message: 'Unavailable' }, { status: 500, statusText: 'Error' });
    expect(session.user()).toEqual(sampleUser);
  });

  it('does not attach the session token to an external resource', () => {
    sessionStorage.setItem('datagram.token', 'signed');
    TestBed.inject(HttpClient)
      .get('https://external.example/resource')
      .subscribe();
    const request = http.expectOne('https://external.example/resource');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
  });
});
