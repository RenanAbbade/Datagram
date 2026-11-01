import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Session, User } from './models';
@Injectable({providedIn:'root'})
export class SessionService {
 private http=inject(HttpClient); private router=inject(Router);
 private current=signal<User|null>(null); readonly user=this.current.asReadonly();
 readonly authenticated=computed(()=>this.user()!==null);
 token():string|null{return sessionStorage.getItem('datagram.token');}
 setUser(user:User){this.current.set(user);}
 login(email:string,senha:string){return this.http.post<Session>(`${environment.apiUrl}/user/login`,{email,senha}).pipe(tap(session=>{sessionStorage.setItem('datagram.token',session.token);this.setUser(session.usuario);}));}
 restore(){return this.token()?this.http.get<User>(`${environment.apiUrl}/user/userLogado`).pipe(tap(u=>this.setUser(u)),catchError(()=>of(null))):of(null);}
 refresh(){return this.http.get<User>(`${environment.apiUrl}/user/userLogado`).pipe(tap(u=>this.setUser(u)));}
 logout(){sessionStorage.removeItem('datagram.token');this.current.set(null);void this.router.navigateByUrl('/login');}
}
