package com.datagram.datagramweb.Repository;
import com.datagram.datagramweb.Domain.Postagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import java.util.*;
public interface PostagemRepository extends JpaRepository<Postagem,Integer> {
 List<Postagem> findByAutorIdOrderByIdDesc(Integer id);
 Slice<Postagem> findByAutorIdIn(Collection<Integer> ids,Pageable pageable);
 Slice<Postagem> findByAutorId(Integer id,Pageable pageable);
}
