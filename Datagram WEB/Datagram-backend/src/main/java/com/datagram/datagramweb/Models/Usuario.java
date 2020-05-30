package com.datagram.datagramweb.Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;


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

  @JsonIgnore
  @OneToMany(mappedBy = "usuario")
  private List<Postagem> postagem = new ArrayList<>();


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

  public void setPostagem(List<Postagem> postagem) {
    this.postagem = postagem;
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