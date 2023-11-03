package com.datagram.datagramweb.repository;

import com.datagram.datagramweb.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
  @Transactional(readOnly = true)
  Member findByEmail(String email);

  @Query("FROM Usuario WHERE NOME LIKE ?1%")
  List<Member> findByNome(String nome);

  @Query("FROM Usuario WHERE INSTITUICAO LIKE ?1%")
  List<Member> findByInstituicao(String instituicao);

  UserDetails findByLogin(String username);
}