export interface User {
 id: number; nome: string; email: string | null; tipoUsuario: 'Membro' | 'Pesquisador' | 'Gerente';
 escolaridade: string | null; estado: string | null; municipio: string | null;
 instituicao: string | null; linkCv: string | null; fotoPerfil: string | null;
 interesses: string[]; seguidores: number; seguindo: number; posts: number;
}
export interface ProfileInput { nome: string; escolaridade: string; estado: string; municipio: string; instituicao: string; linkCv: string; interesses: string[]; }
export interface Registration { nome: string; email: string; senha: string; dataNasc: string; cpf: string; tipoUsuario: string; escolaridade: string; estado: string; municipio: string; instituicao: string; linkCv: string; dataInicio: string; }
export interface PostInput { titulo: string; subtitulo: string; conteudo: string; url: string; tipoPostagem: string; palavrasChave: string[]; }
export interface Post extends PostInput { id: number; autor: User; date: string; curtida: number; curtido: boolean; temArquivo: boolean; }
export interface PostPage { content: Post[]; hasNext: boolean; }
export interface Session { token: string; usuario: User; }
