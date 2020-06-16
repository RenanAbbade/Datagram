package com.datagram.datagramweb.Repositories;

import java.util.List;

import com.datagram.datagramweb.Models.Postagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PostagemRepository extends JpaRepository<Postagem, Integer> {

}
