package com.datagram.datagramweb.Repository;

import com.datagram.datagramweb.Domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
  @Transactional(readOnly = true)
  Usuario findByEmail(String email);

  @Query("SELECT u FROM Usuario u WHERE u.nome LIKE ?1%")
  List<Usuario> findByNome(String nome);

  @Query("SELECT u FROM Usuario u WHERE u.instituicao LIKE ?1%")
  List<Usuario> findByInstituicao(String instituicao);
  
}