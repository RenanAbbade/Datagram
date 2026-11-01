import { Injectable } from '@angular/core';
@Injectable({providedIn:'root'})
export class FileService {
 validate(file:File,kind:'image'|'pdf'):void {
  const types=kind==='image'?['image/png','image/jpeg','image/webp']:['application/pdf'];
  const limit=(kind==='image'?2:5)*1024*1024;
  if(!types.includes(file.type)||file.size>limit)throw new Error(kind==='image'?'Use PNG, JPEG ou WebP de até 2 MB.':'Use um PDF de até 5 MB.');
 }
 download(blob:Blob,name:string){const url=URL.createObjectURL(blob);const a=document.createElement('a');a.href=url;a.download=name;a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);}
}
