package com.datagram.datagramweb.Repositories;

import com.datagram.datagramweb.Models.Postagem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostagemRepository extends JpaRepository<Postagem, Integer> {
}
