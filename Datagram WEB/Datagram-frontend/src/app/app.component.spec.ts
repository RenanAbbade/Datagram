import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AppModule } from './app.module';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppModule],
    }).compileComponents();
  });

  it('creates the application', () => {
    expect(TestBed.createComponent(AppComponent).componentInstance).toBeTruthy();
  });

  it('keeps the Datagram title', () => {
    expect(TestBed.createComponent(AppComponent).componentInstance.title).toBe('Datagram');
  });

  it('renders the router outlet', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('router-outlet')).toBeTruthy();
  });

  it('renders login through the real routes and updates form validation', async () => {
    const fixture = TestBed.createComponent(AppComponent);
    await TestBed.inject(Router).navigateByUrl('/');
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    const root: HTMLElement = fixture.nativeElement;
    const button = root.querySelector<HTMLButtonElement>('.btnSubmit');
    expect(button).toBeTruthy();
    expect(button.disabled).toBe(true);
    for (const [id, value] of [['email', 'test@example.com'], ['senha', 'password']]) {
      const input = root.querySelector<HTMLInputElement>('#' + id);
      input.value = value;
      input.dispatchEvent(new Event('input'));
    }
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(button.disabled).toBe(false);
  });

});
