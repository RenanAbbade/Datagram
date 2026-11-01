import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApiService, errorMessage } from '../core/api.service';
import { User } from '../core/models';
@Component({imports:[RouterLink],template:`<section class="card stack"><h1>Gestão</h1>@if(error()){<p role="alert" class="error">{{error()}}</p>}<p>Média de seguidores: {{average()}}</p><h2>Usuários mais conectados</h2>@for(user of users();track user.id){<a [routerLink]="['/usuarios',user.id]">{{user.nome}} · {{user.seguidores}} seguidores</a>}</section>`})
export class AdminComponent {
 private api=inject(ApiService);average=signal(0);users=signal<User[]>([]);error=signal('');
 ngOnInit(){forkJoin({average:this.api.average(),users:this.api.connected()}).subscribe({next:r=>{this.average.set(r.average);this.users.set(r.users);},error:e=>this.error.set(errorMessage(e))});}
}
