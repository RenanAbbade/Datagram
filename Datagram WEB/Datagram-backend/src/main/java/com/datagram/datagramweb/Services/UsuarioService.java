package com.datagram.datagramweb.Services;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.datagram.datagramweb.Models.Usuario;
import com.datagram.datagramweb.Repositories.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

  public static Usuario usuarioLogado;

  @Autowired
  private UsuarioRepository repo;

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
    //settando os seguidores e o seguindo novamente para não perder a informação no update, o usuario vem do front sem guardar os campos idsSeguidores/idsSeguindo
    Usuario oldObj = find(obj.getId());
    obj.setIdsAllSeguidores(oldObj.getIdsSeguidores());
    obj.setIdsAllSeguindo(oldObj.getIdsSeguindo());

    if(usuarioLogado.getId() == obj.getId()){
      //atualizando o usuario logado para refletir no sistema em tempo real.
      usuarioLogado = obj;
    } 
    else{//só ocorre no cenario follow

      if(obj.getSeguidores() != oldObj.getSeguidores()){

        //verifico se o usuário logado deu unfollow no usuario target & verifico se está aumentando ou decrementando o número de seguidores do usuário alvo.
        if (obj.getIdsSeguidores().contains(usuarioLogado.getId())){
          obj.getIdsSeguidores().remove(usuarioLogado.getId());
          usuarioLogado.getIdsSeguindo().remove(obj.getId());
          //Atualiza o num de seguidores e quem esta seguindo em ambos obj
          obj.setSeguidores(obj.getIdsSeguidores().size());
          usuarioLogado.setSeguindo(usuarioLogado.getIdsSeguindo().size());
        }
        //Novo seguidor
        else{
          obj.setIdsSeguidores(usuarioLogado.getId());//settando os seguidores no target
          usuarioLogado.setIdsSeguindo(obj.getId());//settando na lista dos que o user logado esta seguindo, o número dos seguidores e atualizado dentro destes métodos
        }
        repo.save(usuarioLogado);//salvando o user logado.
      }
    }
    return repo.save(obj);//salvando o objeto passado no parametro.
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

  // Verifica se tentativa de login é válida
  public Usuario login(String email, String senha) {
    Usuario obj = repo.findByEmail(email);
    if (obj == null) {
      return null;
    }

    else if (!obj.getSenha().equalsIgnoreCase(senha)) {
      return null;
    }

    usuarioLogado = obj;

    return obj;
  }

  public boolean validaExistenciaEmail(String email) {
    Usuario obj = repo.findByEmail(email);
    if (obj == null)// Ainda não existe esse email no BD
      return false;
    return true;
  }

  public static synchronized Usuario getUsuarioLogado(){
    return usuarioLogado;
  }

  public List<Usuario> findByNome(String nome){
    List<Usuario> usuariosPesquisa =  repo.findByNome(nome);

    usuariosPesquisa.removeIf(x -> x.getId() == usuarioLogado.getId());//irá remover o usuario logado do retorno da pesquisa

    return usuariosPesquisa;
  }

  public List<Usuario> findByInstituicao(String instituicao){
    return repo.findByInstituicao(instituicao);
  }

  

}