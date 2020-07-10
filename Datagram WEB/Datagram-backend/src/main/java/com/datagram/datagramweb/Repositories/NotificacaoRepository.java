package com.datagram.datagramweb.Repositories;

import com.datagram.datagramweb.Models.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {

}
