package com.datagram.datagramweb.Repositories;

import java.util.List;

import com.datagram.datagramweb.Models.Postagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PostagemRepository extends JpaRepository<Postagem, Integer> {

}
