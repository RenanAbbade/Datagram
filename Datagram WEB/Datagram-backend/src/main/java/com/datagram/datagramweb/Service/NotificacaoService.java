package com.datagram.datagramweb.Service;

import com.datagram.datagramweb.Domain.Postagem;
import com.datagram.datagramweb.Repository.PostagemRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificacaoService {
  private final PostagemRepository posts;

  public NotificacaoService(PostagemRepository posts) {
    this.posts = posts;
  }

  public List<Postagem> findAllbyFollowerId(Integer id) {
    return posts.notifications(id, PageRequest.of(0, 50));
  }
}
