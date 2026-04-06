package com.datagram.datagramweb.Service;

import java.util.*;

import com.datagram.datagramweb.Domain.Usuario;
import com.datagram.datagramweb.Repository.UsuarioRepository;
import com.datagram.datagramweb.config.JWTUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;

    @Autowired
    private JWTUtil jwtUtil;

    public UsuarioService(UsuarioRepository repo) {
        this.repo = repo;
    }

    // CREATE
    public Usuario insert(Usuario obj) {
        obj.setId(null);
        return repo.save(obj);
    }

    // READ
    public Usuario find(Integer id) {
        Optional<Usuario> obj = repo.findById(id);
        return obj.orElse(null);
    }

    // READ ALL
    public Iterable<Usuario> findAll() {
        return repo.findAll();
    }

    // UPDATE
    public Usuario update(Usuario obj) {
        // settando os seguidores e o seguindo novamente para não perder a informação no
        // update, o usuario vem do front sem guardar os campos
        // idsSeguidores/idsSeguindo
        Usuario oldObj = find(obj.getId());
        obj.setIdsAllSeguidores(oldObj.getIdsSeguidores());
        obj.setIdsAllSeguindo(oldObj.getIdsSeguindo());

        Usuario authenticatedUser = authenticated();
        if (authenticatedUser.getId().equals(obj.getId())) {
            // atualizando o usuario logado para refletir no sistema em tempo real.
            // Como não há mais estado estático, isso pode ser removido ou ajustado se necessário
        } else {// só ocorre no cenario follow

            if (obj.getSeguidores() != null && oldObj.getSeguidores() != null && !obj.getSeguidores().equals(oldObj.getSeguidores())) {

                // verifico se o usuário logado deu unfollow no usuario target & verifico se
                // está aumentando ou decrementando o número de seguidores do usuário alvo.
                if (obj.getIdsSeguidores().contains(authenticatedUser.getId())) {
                    obj.getIdsSeguidores().remove(authenticatedUser.getId());
                    authenticatedUser.getIdsSeguindo().remove(obj.getId());
                    // Atualiza o num de seguidores e quem esta seguindo em ambos obj
                    obj.setSeguidores(obj.getIdsSeguidores().size());
                    authenticatedUser.setSeguindo(authenticatedUser.getIdsSeguindo().size());
                }
                // Novo seguidor
                else {
                    obj.setIdsSeguidores(authenticatedUser.getId());// settando os seguidores no target
                    authenticatedUser.setIdsSeguindo(obj.getId());// settando na lista dos que o user logado esta seguindo, o número
                                                             // dos seguidores e atualizado dentro destes métodos
                }
                repo.save(authenticatedUser);// salvando o user logado.
            }
        }
        return repo.save(obj);// salvando o objeto passado no parametro.
    }

    // DELETE
    public void delete(Integer id) {

        find(id);
        try {
            repo.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Não é possível excluir essa entidade!");
        }
    }

    // Verifica se tentativa de login é válida e retorna token JWT
    public Map<String, String> login(String email, String senha) {
        Usuario obj = repo.findByEmail(email);
        if (obj == null) {
            throw new RuntimeException("Usuário não encontrado");
        } else if (!obj.getSenha().equalsIgnoreCase(senha)) {
            throw new RuntimeException("Senha inválida");
        }

        String token = jwtUtil.generateToken(obj);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return response;
    }

    public boolean validaExistenciaEmail(String email) {
        Usuario obj = repo.findByEmail(email);
        if (obj == null)// Ainda não existe esse email no BD
            return false;
        return true;
    }

    // Método auxiliar para obter o usuário autenticado via SecurityContextHolder
    public Usuario authenticated() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            return repo.findByEmail(username);
        } catch (Exception e) {
            throw new RuntimeException("Usuário não autenticado");
        }
    }

    public Usuario findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public List<Usuario> findByNome(String nome) {
        List<Usuario> usuariosPesquisa = repo.findByNome(nome);

        Usuario authenticatedUser = authenticated();
        usuariosPesquisa.removeIf(x -> x.getId().equals(authenticatedUser.getId()));// irá remover o usuario logado do retorno da
                                                                        // pesquisa

        return usuariosPesquisa;
    }

    public List<Usuario> findByInstituicao(String instituicao) {
        List<Usuario> usuariosPesquisa = repo.findByInstituicao(instituicao);
        Usuario authenticatedUser = authenticated();
        usuariosPesquisa.removeIf(x -> x.getId().equals(authenticatedUser.getId()));

        return usuariosPesquisa;
    }

    public List<Usuario> findUsersByInteresses() {
        Usuario authenticatedUser = authenticated();
        Set<String> interessesLogado = authenticatedUser.getInteresses();
        Set<Integer> seguindoLogado = authenticatedUser.getIdsSeguindo();
        Set<Integer> seguidoresLogado = authenticatedUser.getIdsSeguidores();

        List<Usuario> usuarios = repo.findAll();
        List<Usuario> usuariosMutuos = new ArrayList<Usuario>();

        usuarios.removeIf(x -> x.getId().equals(authenticatedUser.getId()));

        for (Usuario user : usuarios) {
            if (!checkFollower(user.getId())) {
                if (CollectionUtils.containsAny(interessesLogado, user.getInteresses())
                        && CollectionUtils.containsAny(seguindoLogado, user.getIdsSeguindo())
                        && CollectionUtils.containsAny(seguidoresLogado, user.getIdsSeguidores())) {
                    usuariosMutuos.add(user);
                    if (usuariosMutuos.size() == 3)
                        break;
                }
            }
        }
        return usuariosMutuos;
    }

    public Boolean checkFollower(Integer id) {
        Usuario authenticatedUser = authenticated();
        Set<Integer> seguindoLogado = authenticatedUser.getIdsSeguindo();
        return seguindoLogado.contains(id);
    }

    public Integer numeroMedioSeguidores() {
        List<Usuario> usuarios = repo.findAll();
        Integer totalSeguidores = 0;
        for (Usuario usuario : usuarios) {
            totalSeguidores += usuario.getSeguidores() != null ? usuario.getSeguidores() : 0;
        }
        return (usuarios.size() != 0 && totalSeguidores != 0) ? (totalSeguidores / usuarios.size()) : 0;
    }

    public List<Usuario> membrosMaisConectados() {
        Map<Usuario, Integer> usuariosSeguidores = new HashMap<>();

        List<Usuario> usuarios = (!repo.findAll().isEmpty()) ? sortUserByNumberOfFollowers(repo.findAll()) : new ArrayList<>();
        List<Usuario> usuariosMaisConectados = new ArrayList<>();

        if (usuarios.size() > 10) {// iterator performa melhor que o for até o list.size()
            Iterator<Usuario> i = usuarios.iterator();

            while (i.hasNext() && usuariosMaisConectados.size() < 11)
                usuariosMaisConectados.add(i.next());

        } else
            return usuariosMaisConectados = usuarios;

        return usuariosMaisConectados;
    }

    public List<Usuario> sortUserByNumberOfFollowers(List<Usuario> myList) {
        myList.sort(new Comparator<Usuario>() {
            @Override
            public int compare(Usuario usuario1, Usuario usuario2) {
                if (usuario1.getSeguidores() == null || usuario2.getSeguidores() == null)
                      return 0;
                return usuario1.getSeguidores().compareTo(usuario2.getSeguidores());
            }
        });
        Collections.reverse(myList);
        return myList;
    }

}
