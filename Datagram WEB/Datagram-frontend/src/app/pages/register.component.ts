import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiService, errorMessage } from '../core/api.service';
@Component({imports:[ReactiveFormsModule,RouterLink],template:`<main class="auth card stack"><h1>Criar conta</h1>
<form [formGroup]="form" (ngSubmit)="register()" class="stack">
<label>Nome<input formControlName="nome" maxlength="120" autocomplete="name" required></label>
<label>Email<input formControlName="email" type="email" autocomplete="email" required></label>
<label>Senha (8 a 72 caracteres)<input formControlName="senha" type="password" minlength="8" maxlength="72" autocomplete="new-password" required></label>
<label>Data de nascimento<input formControlName="dataNasc" type="date" required></label>
<label>Tipo<select formControlName="tipoUsuario"><option value="Membro">Membro</option><option value="Pesquisador">Pesquisador</option></select></label>
@if(form.controls.tipoUsuario.value==='Membro'){<label>CPF<input formControlName="cpf" maxlength="14" required></label>}
@else{<label>Instituição<input formControlName="instituicao" maxlength="200" required></label><label>Início na instituição<input formControlName="dataInicio" type="date" required></label><label>Currículo (URL)<input formControlName="linkCv" type="url"></label>}
<label>Escolaridade<input formControlName="escolaridade" maxlength="120"></label>
@if(error()){<p role="alert" class="error">{{error()}}</p>}
<button type="submit" [disabled]="form.invalid || busy()">{{busy()?'Cadastrando…':'Cadastrar'}}</button></form><a routerLink="/login">Já tenho conta</a></main>`})
export class RegisterComponent {
 private fb=inject(FormBuilder);private api=inject(ApiService);private router=inject(Router);busy=signal(false);error=signal('');
 form=this.fb.nonNullable.group({nome:['',[Validators.required,Validators.maxLength(120)]],email:['',[Validators.required,Validators.email]],senha:['',[Validators.required,Validators.minLength(8),Validators.maxLength(72)]],dataNasc:['',Validators.required],cpf:[''],tipoUsuario:['Membro'],escolaridade:[''],estado:[''],municipio:[''],instituicao:[''],linkCv:[''],dataInicio:['']});
 register(){if(this.form.invalid||this.busy())return;this.error.set('');const d=this.form.getRawValue();
 if(d.tipoUsuario==='Membro'&&!d.cpf.trim()){this.error.set('Informe o CPF.');return;}
 if(d.tipoUsuario==='Pesquisador'&&(!d.dataInicio||!d.instituicao.trim())){this.error.set('Informe a instituição e a data de início.');return;}
 this.busy.set(true);this.api.register(d).pipe(finalize(()=>this.busy.set(false))).subscribe({next:()=>void this.router.navigateByUrl('/login'),error:e=>this.error.set(errorMessage(e))});}
}
