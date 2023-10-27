package com.datagram.datagramweb.repository;

import com.datagram.datagramweb.domain.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {

}
