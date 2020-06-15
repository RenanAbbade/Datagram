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
    if(usuarioLogado.getId() == obj.getId()){
      usuarioLogado = obj;
    } 
    else{
      Usuario oldObj = find(obj.getId());
      //verifico se está aumentando ou decrementando o número de seguidores do usuário alvo.
      if(obj.getSeguidores() != oldObj.getSeguidores()){

        for(Integer id : oldObj.getIdsSeguidores()){//verifico se o usuário logado deu unfollow no usuario target
          if(id == usuarioLogado.getId()){
            obj.getIdsSeguidores().remove(usuarioLogado.getId()); //Achou no SET, e dá o unfollow
            usuarioLogado.getIdsSeguindo().remove(obj.getId());// unfollow registrado no user logado
          }
        }
          //Se não achou no set, é um novo seguidor
        obj.setIdsSeguidores(usuarioLogado.getId());//settando os seguidores no targer
        usuarioLogado.setIdsSeguindo(obj.getId());//settando na lista dos que o user logado esta seguindo
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