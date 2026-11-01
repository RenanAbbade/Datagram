import { inject, Injectable } from '@angular/core';
import {
  HttpClient,
  HttpErrorResponse,
  HttpParams,
} from '@angular/common/http';
import { environment } from '../../environments/environment';
import {
  Post,
  PostInput,
  PostPage,
  ProfileInput,
  Registration,
  User,
} from './models';
export function errorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse)
    return typeof error.error?.message === 'string'
      ? error.error.message
      : error.status === 0
        ? 'Sem conexão com o servidor. Tente novamente.'
        : error.status === 403
          ? 'Você não tem permissão para esta operação.'
          : 'Não foi possível concluir a operação.';
  return error instanceof Error
    ? error.message
    : 'Não foi possível concluir a operação.';
}
@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private base = environment.apiUrl;
  register(data: Registration) {
    return this.http.post<User>(`${this.base}/user/insert`, data);
  }
  user(id: number) {
    return this.http.get<User>(`${this.base}/user/${id}`);
  }
  profile(data: ProfileInput) {
    return this.http.put<User>(`${this.base}/user`, data);
  }
  photo(file: File) {
    const body = new FormData();
    body.append('file', file);
    return this.http.post<User>(`${this.base}/user/foto`, body);
  }
  follow(id: number, follow: boolean) {
    return follow
      ? this.http.put<User>(`${this.base}/user/${id}/follow`, {})
      : this.http.delete<User>(`${this.base}/user/${id}/follow`);
  }
  following(id: number) {
    return this.http.get<boolean>(`${this.base}/user/seguindo/${id}`);
  }
  people(kind: 'seguidores' | 'seguindo' | 'interesses') {
    return this.http.get<User[]>(`${this.base}/user/${kind}`);
  }
  search(value: string, kind: 'nome' | 'instituicao') {
    return this.http.get<User[]>(`${this.base}/user/${kind}`, {
      params: new HttpParams().set(`${kind}_value`, value),
    });
  }
  posts(page = 0, author?: number) {
    let params = new HttpParams().set('page', page).set('size', 20);
    if (author !== undefined) params = params.set('autor', author);
    return this.http.get<PostPage>(`${this.base}/post/feed`, { params });
  }
  create(data: PostInput, file: File | null) {
    if (!file) return this.http.post<Post>(`${this.base}/post/insert`, data);
    const form = new FormData();
    form.append(
      'post',
      new Blob([JSON.stringify(data)], { type: 'application/json' }),
    );
    form.append('file', file);
    return this.http.post<Post>(`${this.base}/post/insert`, form);
  }
  update(id: number, data: PostInput) {
    return this.http.put<Post>(`${this.base}/post/${id}`, data);
  }
  like(post: Post) {
    return post.curtido
      ? this.http.delete<Post>(`${this.base}/post/${post.id}/like`)
      : this.http.put<Post>(`${this.base}/post/${post.id}/like`, {});
  }
  delete(id: number) {
    return this.http.delete<void>(`${this.base}/post/${id}`);
  }
  attachment(id: number) {
    return this.http.get(`${this.base}/post/${id}/arquivo`, {
      responseType: 'blob',
    });
  }
  notifications() {
    return this.http.get<Post[]>(`${this.base}/notificacao/`);
  }
  popular() {
    return this.http.get<Post[]>(`${this.base}/post/maisCurtido`);
  }
  connected() {
    return this.http.get<User[]>(`${this.base}/gerente/maisConectados`);
  }
  average() {
    return this.http.get<number>(`${this.base}/gerente/mediaSeguidores`);
  }
  municipalities(uf: string) {
    return this.http.post<string[]>(`${this.base}/municipios`, uf);
  }
}
