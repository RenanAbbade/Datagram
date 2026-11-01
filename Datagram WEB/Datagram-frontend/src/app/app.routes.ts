import { Routes } from '@angular/router';
import { authGuard, adminGuard } from './core/auth';
export const routes:Routes=[
 {path:'login',loadComponent:()=>import('./pages/login.component').then(m=>m.LoginComponent)},
 {path:'cadastro',loadComponent:()=>import('./pages/register.component').then(m=>m.RegisterComponent)},
 {path:'',canActivate:[authGuard],loadComponent:()=>import('./layout.component').then(m=>m.LayoutComponent),children:[
  {path:'',pathMatch:'full',redirectTo:'feed'},
  {path:'feed',loadComponent:()=>import('./pages/feed.component').then(m=>m.FeedComponent)},
  {path:'perfil',loadComponent:()=>import('./pages/profile.component').then(m=>m.ProfileComponent)},
  {path:'usuarios/:id',loadComponent:()=>import('./pages/profile.component').then(m=>m.ProfileComponent)},
  {path:'buscar',loadComponent:()=>import('./pages/search.component').then(m=>m.SearchComponent)},
  {path:'publicar',loadComponent:()=>import('./pages/compose.component').then(m=>m.ComposeComponent)},
  {path:'notificacoes',loadComponent:()=>import('./pages/notifications.component').then(m=>m.NotificationsComponent)},
  {path:'gerente',canActivate:[adminGuard],loadComponent:()=>import('./pages/admin.component').then(m=>m.AdminComponent)},
  {path:'feed/perfil-amigo/:id',redirectTo:'usuarios/:id'},
  {path:'perfil/perfil-amigo/:id',redirectTo:'usuarios/:id'},
  {path:'home',redirectTo:'feed'}
 ]},
 {path:'**',loadComponent:()=>import('./pages/not-found.component').then(m=>m.NotFoundComponent)}
];
