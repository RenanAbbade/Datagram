package com.datagram.datagramweb.Repositories;

import com.datagram.datagramweb.Models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
  @Transactional(readOnly = true)
  Usuario findByEmail(String email);

}