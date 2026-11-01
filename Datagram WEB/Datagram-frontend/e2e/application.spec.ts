import { test, expect, Page, APIRequestContext } from '@playwright/test';
const password='Password123!';
const png=Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+j3ioAAAAASUVORK5CYII=','base64');
async function login(page:Page,email:string){await page.goto('/login');await page.getByLabel('Email',{exact:true}).fill(email);await page.getByLabel('Senha',{exact:true}).fill(password);await page.getByRole('button',{name:'Entrar',exact:true}).click();await expect(page).toHaveURL(/\/feed$/);}
async function account(request:APIRequestContext,name:string){const email=`${name}-${Date.now()}-${Math.random()}@example.com`;const result=await request.post('/api/user/insert',{data:{nome:name,email,senha:password,dataNasc:'1990-01-01',tipoUsuario:'Pesquisador',instituicao:'Universidade',dataInicio:'2015-01-01'}});expect(result.status()).toBe(201);const user=await result.json();const auth=await request.post('/api/user/login',{data:{email,senha:password}});return {email,id:user.id,token:(await auth.json()).token};}

test('register, login, upload photo, publish/edit/PDF/delete and navigate without F5',async({page})=>{
 const email=`ui-${Date.now()}@example.com`;const errors:string[]=[];page.on('pageerror',e=>errors.push(e.message));
 await page.goto('/cadastro');await page.getByLabel('Nome',{exact:true}).fill('Pesquisador UI');await page.getByLabel('Email',{exact:true}).fill(email);await page.getByLabel('Senha (8 a 72 caracteres)').fill(password);await page.getByLabel('Data de nascimento').fill('1990-01-01');await page.getByLabel('Tipo',{exact:true}).selectOption('Pesquisador');await page.getByLabel('Instituição',{exact:true}).fill('Universidade');await page.getByLabel('Início na instituição').fill('2015-01-01');await page.getByRole('button',{name:'Cadastrar',exact:true}).click();await expect(page).toHaveURL(/\/login$/);
 await login(page,email);
 await page.evaluate(()=>{(window as unknown as {spaMarker:string}).spaMarker='persistent';});
 await page.getByRole('link',{name:'Meu perfil'}).click();await expect(page.getByRole('heading',{name:'Pesquisador UI',exact:true})).toBeVisible();
 await page.getByLabel('Alterar foto').setInputFiles({name:'avatar.png',mimeType:'image/png',buffer:png});
 const headerPhoto=page.locator('header.topbar app-avatar img');await expect(headerPhoto).toHaveAttribute('src',/\/api\/user\/\d+\/foto\?v=/);
 await expect(page.locator('section app-avatar img').first()).toHaveAttribute('src',await headerPhoto.getAttribute('src')||'');
 await page.getByRole('button',{name:'Editar perfil',exact:true}).click();await page.getByLabel('Nome',{exact:true}).fill('Nome atualizado');await page.getByRole('button',{name:'Salvar perfil'}).click();await expect(page.getByRole('heading',{name:'Nome atualizado',exact:true})).toBeVisible();
 await page.getByRole('link',{name:'Publicar',exact:true}).click();await page.getByLabel('Título',{exact:true}).fill('Minha pesquisa');await page.getByLabel('Conteúdo',{exact:true}).fill('Conteúdo científico');await page.getByLabel('PDF opcional').setInputFiles({name:'pesquisa.pdf',mimeType:'application/pdf',buffer:Buffer.from('%PDF-1.4 test')});await page.getByRole('button',{name:'Salvar publicação'}).click();await expect(page).toHaveURL(/\/feed$/);await expect(page.getByRole('heading',{name:'Minha pesquisa'})).toBeVisible();
 await page.getByRole('button',{name:'Curtir · 0',exact:true}).click();await expect(page.getByRole('button',{name:'Descurtir · 1',exact:true})).toBeVisible();
 await page.getByRole('button',{name:'Editar',exact:true}).click();await page.getByLabel('Título',{exact:true}).fill('Pesquisa revisada');await page.getByRole('button',{name:'Salvar publicação'}).click();await expect(page.getByRole('heading',{name:'Pesquisa revisada'})).toBeVisible();
 const download=page.waitForEvent('download');await page.getByRole('button',{name:'Baixar PDF'}).click();expect((await download).suggestedFilename()).toMatch(/publicacao-\d+\.pdf/);
 expect(await page.evaluate(()=>(window as unknown as {spaMarker:string}).spaMarker)).toBe('persistent');
 await page.getByRole('button',{name:'Excluir',exact:true}).click();await page.getByRole('button',{name:'Confirmar exclusão'}).click();await expect(page.getByRole('heading',{name:'Pesquisa revisada'})).toHaveCount(0);
 await page.screenshot({path:'test-results/feed-desktop.png',fullPage:true});
 await page.getByRole('button',{name:'Sair',exact:true}).click();await expect(page).toHaveURL(/\/login$/);await page.goto('/perfil');await expect(page).toHaveURL(/\/login$/);expect(errors).toEqual([]);
});

test('search, follow, notifications and route parameter changes',async({page,request})=>{
 const first=await account(request,'Ana Pesquisadora');const second=await account(request,'Bruno Pesquisador');const viewer=await account(request,'Leitor');await login(page,viewer.email);
 await page.getByRole('link',{name:'Buscar',exact:true}).click();await page.getByLabel('Nome ou instituição').fill('Ana Pesquisadora');await page.getByRole('link',{name:/Ana Pesquisadora/}).click();await expect(page.getByRole('heading',{name:'Ana Pesquisadora'})).toBeVisible();await page.getByRole('button',{name:'Seguir',exact:true}).click();await expect(page.getByRole('button',{name:'Deixar de seguir'})).toBeVisible();
 const result=await request.post('/api/post/insert',{headers:{Authorization:`Bearer ${first.token}`},data:{titulo:'Publicação seguida',conteudo:'Conhecimento',tipoPostagem:'simples',palavrasChave:[]}});expect(result.status()).toBe(201);
 await page.getByRole('link',{name:'Feed',exact:true}).click();await expect(page.getByRole('heading',{name:'Publicação seguida'})).toBeVisible();await page.getByRole('link',{name:'Notificações',exact:true}).click();await expect(page.getByRole('heading',{name:'Publicação seguida'})).toBeVisible();
 await page.goto(`/usuarios/${first.id}`);await expect(page.getByRole('heading',{name:'Ana Pesquisadora'})).toBeVisible();
 // SPA navigation to another parameter must replace the displayed profile.
 await page.getByRole('link',{name:'Buscar',exact:true}).click();await page.getByLabel('Nome ou instituição').fill('Bruno Pesquisador');await page.getByRole('link',{name:/Bruno Pesquisador/}).click();await expect(page).toHaveURL(new RegExp(`/usuarios/${second.id}$`));await expect(page.getByRole('heading',{name:'Bruno Pesquisador'})).toBeVisible();
 await page.goto('/gerente');await expect(page).toHaveURL(/\/feed$/);
});

test('failed photo saves retain the current image and mobile layout fits',async({page,request})=>{
 const a=await account(request,'Foto teste');await page.setViewportSize({width:390,height:844});await login(page,a.email);await page.getByRole('link',{name:'Meu perfil'}).click();const photo=page.locator('header.topbar app-avatar img');const before=await photo.getAttribute('src');
 await page.route('**/api/user/foto',route=>route.fulfill({status:500,contentType:'application/json',body:JSON.stringify({message:'Falha simulada'})}));await page.getByLabel('Alterar foto').setInputFiles({name:'a.png',mimeType:'image/png',buffer:png});await expect(page.getByRole('alert')).toHaveText('Falha simulada');await expect(photo).toHaveAttribute('src',before||'');
 await page.unroute('**/api/user/foto');await page.getByLabel('Alterar foto').setInputFiles({name:'a.png',mimeType:'image/png',buffer:png});await expect(photo).toHaveAttribute('src',/\/foto\?v=/);expect(await page.evaluate(()=>document.documentElement.scrollWidth<=window.innerWidth)).toBe(true);await page.screenshot({path:'test-results/profile-mobile.png',fullPage:true});
});
