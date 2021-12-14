package com.datagram.datagramweb.Repository;

import com.datagram.datagramweb.Domain.Postagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PostagemRepository extends JpaRepository<Postagem, Integer> {

}
