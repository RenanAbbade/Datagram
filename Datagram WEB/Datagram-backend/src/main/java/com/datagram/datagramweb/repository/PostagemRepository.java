package com.datagram.datagramweb.repository;

import com.datagram.datagramweb.domain.Postagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PostagemRepository extends JpaRepository<Postagem, Integer> {

}
