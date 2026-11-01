package com.datagram.datagramweb.Repository;

import com.datagram.datagramweb.Domain.Postagem;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostagemRepository extends JpaRepository<Postagem, Integer> {
  List<Postagem> findTop50ByAutorIdOrderByIdDesc(Integer id);

  List<Postagem> findTop10ByAutorIdOrderByCurtidaDescIdDesc(Integer id);

  Slice<Postagem> findByAutorIdIn(Collection<Integer> ids, Pageable pageable);

  Slice<Postagem> findByAutorId(Integer id, Pageable pageable);

  @org.springframework.data.jpa.repository.Query(
      "select p from Postagem p where p.id in (select n.postagem_id from Notificacao n where"
          + " n.usuario.id = :id) order by p.id desc")
  List<Postagem> notifications(
      @org.springframework.data.repository.query.Param("id") Integer id, Pageable pageable);
}
