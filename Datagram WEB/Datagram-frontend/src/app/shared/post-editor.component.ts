import { Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiService, errorMessage } from '../core/api.service';
import { Post } from '../core/models';
import { FileService } from './file.service';
@Component({selector:'app-post-editor',imports:[ReactiveFormsModule],template:`
<form [formGroup]="form" (ngSubmit)="save()" class="stack">
 <label>Título<input formControlName="titulo" maxlength="200" required></label>
 <label>Subtítulo<input formControlName="subtitulo" maxlength="300"></label>
 <label>Conteúdo<textarea formControlName="conteudo" rows="5" maxlength="20000" required></textarea></label>
 <label>Tipo<select formControlName="tipoPostagem"><option value="simples">Postagem simples</option><option value="cientifica">Publicação científica</option></select></label>
 <label>Link da publicação<input formControlName="url" type="url" placeholder="https://..."></label>
 <label>Palavras-chave, separadas por vírgula<input formControlName="tags" maxlength="1000"></label>
 @if(!post()){<label>PDF opcional, até 5 MB<input type="file" accept="application/pdf" (change)="selectFile($event)"></label>}
 @if(error()){<p role="alert" class="error">{{error()}}</p>}
 <button type="submit" [disabled]="form.invalid || busy()">{{busy()?'Salvando…':'Salvar publicação'}}</button>
</form>`})
export class PostEditorComponent {
 private api=inject(ApiService);private files=inject(FileService);private fb=inject(FormBuilder);
 post=input<Post|null>(null);saved=output<Post>();busy=signal(false);error=signal('');file:File|null=null;invalidFile=false;
 form=this.fb.nonNullable.group({titulo:['',[Validators.required,Validators.maxLength(200)]],subtitulo:[''],conteudo:['',[Validators.required,Validators.maxLength(20000)]],tipoPostagem:['simples'],url:[''],tags:['']});
 ngOnInit(){const p=this.post();if(p)this.form.patchValue({...p,tags:p.palavrasChave.join(', ')});}
 selectFile(event:Event){const file=(event.target as HTMLInputElement).files?.[0];this.file=null;this.invalidFile=false;this.error.set('');if(!file)return;try{this.files.validate(file,'pdf');this.file=file;}catch(e){this.invalidFile=true;this.error.set(errorMessage(e));}}
 save(){if(this.form.invalid||this.busy()||this.invalidFile)return;this.busy.set(true);this.error.set('');
  const {tags,...v}=this.form.getRawValue();const data={...v,palavrasChave:tags.split(',').map(t=>t.trim()).filter(Boolean)};
  const p=this.post();(p?this.api.update(p.id,data):this.api.create(data,this.file)).pipe(finalize(()=>this.busy.set(false))).subscribe({next:saved=>this.saved.emit(saved),error:e=>this.error.set(errorMessage(e))});
 }
}
