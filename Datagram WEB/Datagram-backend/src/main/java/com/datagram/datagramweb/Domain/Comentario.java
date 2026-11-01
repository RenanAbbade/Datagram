package com.datagram.datagramweb.Domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import java.io.Serializable;
import java.util.Date;

@Embeddable
public class Comentario implements Serializable {

  private static final long serialVersionUID = 1L;

  @Lob private Usuario autor;
  private String conteudo;
  private Date date;

  public Comentario() {}

  public Comentario(Usuario autor, String conteudo, Date date) {
    this.autor = autor;
    this.conteudo = conteudo;
    this.date = date;
  }

  public Usuario getUsuario() {
    return autor;
  }

  public void setUsuario(Usuario usuario) {
    this.autor = usuario;
  }

  public String getConteudo() {
    return conteudo;
  }

  public void setConteudo(String conteudo) {
    this.conteudo = conteudo;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}
