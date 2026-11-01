import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import {
  Component,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, finalize, of, switchMap, tap } from 'rxjs';
import { ApiService, errorMessage } from '../core/api.service';
import { SessionService } from '../core/session.service';
import { User } from '../core/models';
@Component({
  selector: 'app-profile-form',
  imports: [ReactiveFormsModule],
  template: `<section class="card stack">
    <h2>Editar perfil</h2>
    <form [formGroup]="form" (ngSubmit)="save()" class="stack">
      <label
        >Nome<input formControlName="nome" maxlength="120" required /></label
      ><label
        >Escolaridade<input formControlName="escolaridade" maxlength="120"
      /></label>
      <label
        >UF<select formControlName="estado">
          <option value="">Selecione</option>
          @for (uf of states; track uf) {
            <option [value]="uf">{{ uf }}</option>
          }
        </select></label
      >
      <label
        >Município<input
          formControlName="municipio"
          list="cities"
          maxlength="120" /><datalist id="cities">
          @for (city of cities(); track city) {
            <option [value]="city"></option>
          }</datalist
      ></label>
      <label
        >Instituição<input
          formControlName="instituicao"
          maxlength="200" /></label
      ><label
        >Currículo (URL)<input type="url" formControlName="linkCv"
      /></label>
      <label
        >Interesses, separados por vírgula<input
          formControlName="tags"
          maxlength="1000"
      /></label>
      @if (error()) {
        <p role="alert" class="error">{{ error() }}</p>
      }
      <button type="submit" [disabled]="formStatus() !== 'VALID' || busy()">
        {{ busy() ? 'Salvando…' : 'Salvar perfil' }}
      </button>
    </form>
  </section>`,
})
export class ProfileFormComponent {
  user = input.required<User>();
  saved = output<User>();
  private api = inject(ApiService);
  private session = inject(SessionService);
  private fb = inject(FormBuilder);
  busy = signal(false);
  error = signal('');
  cities = signal<string[]>([]);
  states =
    'AC AL AP AM BA CE DF ES GO MA MT MS MG PA PB PR PE PI RJ RN RS RO RR SC SP SE TO'.split(
      ' ',
    );
  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    escolaridade: [''],
    estado: [''],
    municipio: [''],
    instituicao: [''],
    linkCv: [''],
    tags: [''],
  });
  formStatus = toSignal(this.form.statusChanges, {
    initialValue: this.form.status,
  });
  constructor() {
    effect(() => {
      const u = this.user();
      this.form.patchValue(
        {
          nome: u.nome,
          escolaridade: u.escolaridade || '',
          estado: u.estado || '',
          municipio: u.municipio || '',
          instituicao: u.instituicao || '',
          linkCv: u.linkCv || '',
          tags: u.interesses.join(', '),
        },
        { emitEvent: false },
      );
      this.form.updateValueAndValidity();
    });
    this.form.controls.estado.valueChanges
      .pipe(
        tap(() => {
          this.cities.set([]);
          this.form.controls.municipio.setValue('');
        }),
        switchMap((uf) =>
          uf
            ? this.api.municipalities(uf).pipe(
                catchError(() => {
                  this.error.set(
                    'Não foi possível listar municípios. Você pode digitar o nome.',
                  );
                  return of([] as string[]);
                }),
              )
            : of([] as string[]),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((cities) => this.cities.set(cities));
  }
  save() {
    if (this.form.invalid || this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    const { tags, ...d } = this.form.getRawValue();
    this.api
      .profile({
        ...d,
        interesses: tags
          .split(',')
          .map((t) => t.trim())
          .filter(Boolean),
      })
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: (u) => {
          this.session.setUser(u);
          this.saved.emit(u);
        },
        error: (e) => this.error.set(errorMessage(e)),
      });
  }
}
