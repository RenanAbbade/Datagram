package com.datagram.datagramweb.Repositories;

import com.datagram.datagramweb.Models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
  @Transactional(readOnly = true)
  Usuario findByEmail(String email);

  @Query("FROM Usuario WHERE NOME LIKE ?1%")
  List<Usuario> findByNome(String nome);

  @Query("FROM Usuario WHERE INSTITUICAO LIKE ?1%")
  List<Usuario> findByInstituicao(String instituicao);
  
}