import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SessionService } from './core/session.service';
import { AvatarComponent } from './shared/avatar.component';
@Component({selector:'app-layout',imports:[RouterLink,RouterLinkActive,RouterOutlet,AvatarComponent],template:`
<header class="topbar"><a routerLink="/feed" class="brand"><img src="/assets/img/logo2.png" width="36" height="36" alt="">Datagram</a>
<nav aria-label="Principal"><a routerLink="/feed" routerLinkActive="active">Feed</a><a routerLink="/buscar" routerLinkActive="active">Buscar</a><a routerLink="/publicar" routerLinkActive="active">Publicar</a><a routerLink="/notificacoes" routerLinkActive="active">Notificações</a>
@if(session.user()?.tipoUsuario==='Gerente'){<a routerLink="/gerente">Gestão</a>}
<a routerLink="/perfil" class="row"><app-avatar [user]="session.user()"/>Meu perfil</a><button type="button" class="secondary" (click)="session.logout()">Sair</button></nav></header>
<main class="container"><router-outlet/></main>`})
export class LayoutComponent { session=inject(SessionService); }
