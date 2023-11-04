package com.datagram.datagramweb.service;

import com.datagram.datagramweb.domain.Member;
import com.datagram.datagramweb.dto.MemberDTO;
import com.datagram.datagramweb.mapper.MemberMapper;
import com.datagram.datagramweb.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
public class UsuarioService {

  public static Member memberLogado;

  @Autowired
  private MemberRepository repository;

  // CREATE
  public MemberDTO save(MemberDTO memberDTO) {
    Member member = MemberMapper.INSTANCE.memberDTOToMember(memberDTO);
    member.setId(null);
    return  MemberMapper.INSTANCE.memberToMemberDTO(repository.save(member));
  }

  // READ
  public Member find(Integer id) {
    Optional<Member> obj = repository.findById(id);
    return obj.orElse(null);
  }

  // READ ALL
  public Iterable<Member> findAll() {
    return repository.findAll();
  }

  // UPDATE
  public Member update(Member obj) {
    // settando os seguidores e o seguindo novamente para não perder a informação no
    // update, o usuario vem do front sem guardar os campos
    // idsSeguidores/idsSeguindo
    Member oldObj = find(obj.getId());
    obj.setIdsAllSeguidores(oldObj.getIdsSeguidores());
    obj.setIdsAllSeguindo(oldObj.getIdsSeguindo());

    if (memberLogado.getId() == obj.getId()) {
      // atualizando o usuario logado para refletir no sistema em tempo real.
      memberLogado = obj;
    } else {// só ocorre no cenario follow

      if (obj.getSeguidores() != oldObj.getSeguidores()) {

        // verifico se o usuário logado deu unfollow no usuario target & verifico se
        // está aumentando ou decrementando o número de seguidores do usuário alvo.
        if (obj.getIdsSeguidores().contains(memberLogado.getId())) {
          obj.getIdsSeguidores().remove(memberLogado.getId());
          memberLogado.getIdsSeguindo().remove(obj.getId());
          // Atualiza o num de seguidores e quem esta seguindo em ambos obj
          obj.setSeguidores(obj.getIdsSeguidores().size());
          memberLogado.setSeguindo(memberLogado.getIdsSeguindo().size());
        }
        // Novo seguidor
        else {
          obj.setIdsSeguidores(memberLogado.getId());// settando os seguidores no target
          memberLogado.setIdsSeguindo(obj.getId());// settando na lista dos que o user logado esta seguindo, o número
                                                    // dos seguidores e atualizado dentro destes métodos
        }
        repository.save(memberLogado);// salvando o user logado.
      }
    }
    return repository.save(obj);// salvando o objeto passado no parametro.
  }

  // DELETE
  public void delete(Integer id) {

    find(id);
    try {
      repository.deleteById(id);
    } catch (DataIntegrityViolationException e) {
      throw new DataIntegrityViolationException("Não é possível excluir essa entidade!");
    }
  }

  // Verifica se tentativa de login é válida
  public Member login(String email, String senha) {
    Member obj = repository.findByEmail(email);
    if (obj == null) {
      return null;
    } else if (!obj.getSenha().equalsIgnoreCase(senha)) {
      return null;
    }

    memberLogado = obj;

    return obj;
  }

//  public boolean validaExistenciaEmail(String email) {
//    Member obj repository.findByEmail(email);
//    if (obj == null)// Ainda não existe esse email no BD
//      return false;
//    return true;
//  }

  public static synchronized Member getUsuarioLogado() {
    return memberLogado;
  }

  public List<Member> findByNome(String nome) {
    List<Member> usuariosPesquisa = repository.findByNome(nome);

    usuariosPesquisa.removeIf(x -> x.getId() == memberLogado.getId());// irá remover o usuario logado do retorno da
                                                                       // pesquisa

    return usuariosPesquisa;
  }

  public List<Member> findByInstituicao(String instituicao) {
    List<Member> usuariosPesquisa = repository.findByInstituicao(instituicao);
    usuariosPesquisa.removeIf(x -> x.getId().equals(memberLogado.getId()));

    return usuariosPesquisa;
  }

  public List<Member> findUsersByInteresses() {
    Set<String> interessesLogado = UsuarioService.memberLogado.getInteresses();
    Set<Integer> seguindoLogado = UsuarioService.memberLogado.getIdsSeguindo();
    Set<Integer> seguidoresLogado = UsuarioService.memberLogado.getIdsSeguidores();

    List<Member> members = repository.findAll();
    List<Member> usuariosMutuos = new ArrayList<Member>();

    members.removeIf(x -> x.getId().equals(memberLogado.getId()));

    for (Member user : members) {
      if (!checkFollower(user.getId())) {
        if (CollectionUtils.containsAny(interessesLogado, user.getInteresses())
            && CollectionUtils.containsAny(seguindoLogado, user.getIdsSeguindo())
            && CollectionUtils.containsAny(seguidoresLogado, user.getIdsSeguidores())) {
          usuariosMutuos.add(user);
          if (usuariosMutuos.size() == 3) 
            break;
        }
      }
    }
    return usuariosMutuos;
  }

  public Boolean checkFollower(Integer id) {
    Set<Integer> seguindoLogado = UsuarioService.memberLogado.getIdsSeguindo();
    return seguindoLogado.contains(id);
  }

  public Integer followersAverageNumber() {
    List<Member> users = repository.findAll();
    Integer totalFollowers = 0;
    for (Member member : users) {
      totalFollowers += member.getSeguidores() != null ? member.getSeguidores() : 0;
    }
    return (users.size() != 0 && totalFollowers != 0) ? (totalFollowers / users.size()) : 0;
  }

  public List<Member> moreConnectedMembers() {

    List<Member> users = (!repository.findAll().isEmpty()) ? sortUserByNumberOfFollowers(repository.findAll()) : new ArrayList<>();
    List<Member> moreConnectedUsers = new ArrayList<>();

    if (users.size() > 10) {// iterator performa melhor que o for até o list.size()
      Iterator<Member> i = users.iterator();

      while (i.hasNext() && moreConnectedUsers.size() < 11)
        moreConnectedUsers.add(i.next());

    } else 
        return moreConnectedUsers = users;
        
    return moreConnectedUsers;
  }

  public List<Member> sortUserByNumberOfFollowers(List<Member> myList) {
    myList.sort(new Comparator<Member>() {
      @Override
      public int compare(Member member1, Member member2) {
        if (member1.getSeguidores() == null || member2.getSeguidores() == null)
              return 0;
        return member1.getSeguidores().compareTo(member2.getSeguidores());
      }
    });
    Collections.reverse(myList);
    return myList;
  }

}
