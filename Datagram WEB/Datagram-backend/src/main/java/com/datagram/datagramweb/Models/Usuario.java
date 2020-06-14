package com.datagram.datagramweb.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;


@Entity
public class Usuario implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String email;

  private String nome;

  private String senha;

  private String dataNasc;

  private String cpf;

  private String escolaridade;

  private String estado;

  private String municipio;

  private String instituicao;

  private String linkCv;

  private String dataInicio;

  private String tipoUsuario;

  private int seguidores;

  private int seguindo;

  private int posts;

  @Column(columnDefinition = "text")
  private String fotoPerfil;

  @ElementCollection(fetch = FetchType.EAGER) 
  @CollectionTable(name="INTERESSES")//Telefone se torna em uma tabela auxiliar de identididade fraca
  private Set<String> interesses = new HashSet<>();

  @JsonIgnore
  @OneToMany(mappedBy = "autor")
  private List<Postagem> postagem = new ArrayList<>();

  @JsonIgnore
  @ElementCollection(fetch = FetchType.EAGER) 
  @CollectionTable(name="Ids_seguidores")
  private Set<Integer> idsSeguidores = new HashSet<>();

  @JsonIgnore
  @ElementCollection(fetch = FetchType.EAGER) 
  @CollectionTable(name="Id_seguindo")
  private Set<Integer> idsSeguindo = new HashSet<>();

  public Usuario() {
  }
  
  //Pesquisador constructor
  public Usuario(Integer id, String nome, String senha, String email, String dataNasc, String escolaridade, String estado,
      String municipio, String instituicao, String linkCv, String dataInicio, String tipoUsuario) {
    this.id = id;
    this.nome = nome;
    this.senha = senha;
    this.email = email;
    this.dataNasc = dataNasc;
    this.escolaridade = escolaridade;
    this.estado = estado;
    this.municipio = municipio;
    this.instituicao = instituicao;
    this.linkCv = linkCv;
    this.dataInicio = dataInicio;
    this.tipoUsuario = tipoUsuario;
    this.cpf = null;
}

  //membro constructor
  public Usuario(Integer id, String nome, String senha, String email, String cpf, String escolaridade, String tipoUsuario) {
    this.id = id;
    this.nome = nome;
    this.senha = senha;
    this.email = email;
    this.cpf = cpf;
    this.escolaridade = escolaridade;
    this.tipoUsuario = tipoUsuario;
    this.estado = null;
    this.municipio = null;
    this.instituicao = null;
    this.linkCv = null;
    this.dataInicio = null;
  }

  


public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getSenha() {
    return senha;
  }

  public void setSenha(String senha) {
    this.senha = senha;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getDataNasc() {
    return dataNasc;
  }

  public void setDataNasc(String dataNasc) {
    this.dataNasc = dataNasc;
  }

  public String getCpf() {
    return cpf;
  }

  public void setCpf(String cpf) {
    this.cpf = cpf;
  }

  public String getEscolaridade() {
    return escolaridade;
  }

  public void setEscolaridade(String escolaridade) {
    this.escolaridade = escolaridade;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public String getMunicipio() {
    return municipio;
  }

  public void setMunicipio(String municipio) {
    this.municipio = municipio;
  }

  public String getInstituicao() {
    return instituicao;
  }

  public void setInstituicao(String instituicao) {
    this.instituicao = instituicao;
  }

  public String getLinkCv() {
    return linkCv;
  }

  public void setLinkCv(String linkCv) {
    this.linkCv = linkCv;
  }

  public String getDataInicio() {
    return dataInicio;
  }

  public void setDataInicio(String dataInicio) {
    this.dataInicio = dataInicio;
  }

  public String getTipoUsuario() {
    return tipoUsuario;
  }

  public void setTipoUsuario(String tipoUsuario) {
    this.tipoUsuario = tipoUsuario;
  }

  public List<Postagem> getPostagem() {
    return postagem;
  }

  public void setPostagem(Postagem post) {
    this.postagem.add(post);
  }

  public int getSeguidores() {
    return seguidores;
  }

  public void setSeguidores(int seguidores) {
    this.seguidores = seguidores;
  }

  public int getSeguindo() {
    return seguindo;
  }

  public void setSeguindo(int seguindo) {
    this.seguindo = seguindo;
  }

  public void setPosts(){
    this.posts = postagem.size();
  }

  public int getPosts() {
    return posts;
  }

  public String getFotoPerfil() {
    return fotoPerfil;
  }

  public void setFotoPerfil(String fotoPerfil) {
    this.fotoPerfil = fotoPerfil;
  }

  public Set<String> getInteresses() {
    return interesses;
  }

  public void setInteresses(Set<String> interesses) {
    this.interesses = interesses;
  }

  public Set<Integer> getIdsSeguidores() {
    return idsSeguidores;
  }

  public void setIdsSeguidores(Integer idSeguidor) {
    this.idsSeguidores.add(idSeguidor);
    this.seguidores = idsSeguidores.size();
  }

  public Set<Integer> getIdsSeguindo() {
    return idsSeguindo;
  }

  public void setIdsSeguindo(Integer idSeguindo) {
    this.idsSeguindo.add(idSeguindo);
    this.seguindo = idsSeguindo.size();
  }

  

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }



  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Usuario other = (Usuario) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }








}