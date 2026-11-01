package com.datagram.datagramweb.Repository;

import com.datagram.datagramweb.Domain.Usuario;
import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
  Usuario findByEmail(String email);

  List<Usuario> findByNomeStartingWithIgnoreCase(String nome, Pageable page);

  List<Usuario> findByInstituicaoStartingWithIgnoreCase(String instituicao, Pageable page);

  List<Usuario> findTop10ByOrderBySeguidoresDescIdDesc();

  @Query("select coalesce(avg(coalesce(u.seguidores,0)),0) from Usuario u")
  Double averageFollowers();

  @Query(
      "select distinct u from Usuario u join u.interesses i where lower(i) in :interesses and u.id"
          + " <> :id and u.id not in (select s from Usuario me join me.idsSeguindo s where me.id ="
          + " :id)")
  List<Usuario> suggestions(
      @Param("id") Integer id, @Param("interesses") Set<String> interesses, Pageable page);
}
