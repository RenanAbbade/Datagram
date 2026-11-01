package com.datagram.datagramweb.Repository;

import com.datagram.datagramweb.Domain.Notificacao;
import org.springframework.data.jpa.repository.*;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {
  @Modifying
  @Query("delete from Notificacao n where n.postagem_id = :id")
  void deleteForPost(@org.springframework.data.repository.query.Param("id") Integer id);
}
