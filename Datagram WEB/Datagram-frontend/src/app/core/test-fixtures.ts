import { User } from './models';
export const sampleUser: User = {
  id: 1,
  nome: 'Renan',
  email: 'test@example.com',
  tipoUsuario: 'Pesquisador',
  escolaridade: null,
  estado: null,
  municipio: null,
  instituicao: null,
  linkCv: null,
  fotoPerfil: '/api/user/1/foto?v=old',
  interesses: [],
  seguidores: 0,
  seguindo: 0,
  posts: 0,
};
