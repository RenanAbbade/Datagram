import { TestBed } from '@angular/core/testing';
import { provideRouter, ActivatedRoute, convertToParamMap } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { ProfileComponent } from './profile.component';
import { LayoutComponent } from '../layout.component';
import { SessionService } from '../core/session.service';
import { sampleUser } from '../core/test-fixtures';
describe('Photo upload without reloading',()=>{
 let http:HttpTestingController;let session:SessionService;
 beforeEach(()=>{TestBed.configureTestingModule({imports:[ProfileComponent,LayoutComponent],providers:[provideRouter([]),provideHttpClient(),provideHttpClientTesting(),{provide:ActivatedRoute,useValue:{paramMap:of(convertToParamMap({}))}}]});http=TestBed.inject(HttpTestingController);session=TestBed.inject(SessionService);session.setUser(sampleUser);});
 afterEach(()=>http.verify());
 function profile(){const fixture=TestBed.createComponent(ProfileComponent);fixture.detectChanges();http.expectOne('/api/user/1').flush(sampleUser);http.expectOne('/api/post/feed?page=0&size=20&autor=1').flush({content:[],hasNext:false});fixture.detectChanges();return fixture;}
 function event(file?:File){return {target:{files:file?[file]:[],value:'file'}} as unknown as Event;}
 it('changes the image in both profile and persistent header after the server confirms',()=>{
  const f=profile();const header=TestBed.createComponent(LayoutComponent);header.detectChanges();
  f.componentInstance.photo(event(new File(['png'],'photo.png',{type:'image/png'})));
  const r=http.expectOne('/api/user/foto');expect(r.request.body instanceof FormData).toBe(true);
  expect(session.user()?.fotoPerfil).toContain('old');r.flush({...sampleUser,fotoPerfil:'/api/user/1/foto?v=new'});
  f.detectChanges();header.detectChanges();
  expect(f.nativeElement.querySelector('app-avatar img').getAttribute('src')).toContain('new');
  expect(header.nativeElement.querySelector('app-avatar img').getAttribute('src')).toContain('new');
 });
 it('preserves the previous photo after a failed save and allows retry',()=>{
  const f=profile();const file=new File(['png'],'photo.png',{type:'image/png'});f.componentInstance.photo(event(file));http.expectOne('/api/user/foto').flush({message:'Failed'},{status:500,statusText:'Error'});
  expect(session.user()?.fotoPerfil).toContain('old');expect(f.componentInstance.busy()).toBe(false);
  f.componentInstance.photo(event(file));http.expectOne('/api/user/foto').flush({...sampleUser,fotoPerfil:'/api/user/1/foto?v=retry'});expect(session.user()?.fotoPerfil).toContain('retry');
 });
 it('ignores canceled selections and rejects invalid files without an HTTP request',()=>{
  const f=profile();f.componentInstance.photo(event());f.componentInstance.photo(event(new File(['svg'],'photo.svg',{type:'image/svg+xml'})));http.expectNone('/api/user/foto');expect(session.user()?.fotoPerfil).toContain('old');expect(f.componentInstance.error()).toContain('PNG');
 });
});
