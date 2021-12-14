package com.datagram.datagramweb.Repository;

import com.datagram.datagramweb.Domain.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {

}
