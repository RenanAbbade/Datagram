package com.datagram.datagramweb.Services;

import java.util.Optional;

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
    find(obj.getId());
    return repo.save(obj);
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
}