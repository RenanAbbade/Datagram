package com.datagram.datagramweb.domain;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Embeddable
public class Comentario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Lob
    private Member autor;
    private String conteudo;
    private Date date;

    public Comentario() {

    }

    public Comentario(Member autor, String conteudo, Date date) {
        this.autor = autor;
        this.conteudo = conteudo;
        this.date = date;
    }

    public Member getUsuario() {
        return autor;
    }

    public void setUsuario(Member member) {
        this.autor = member;
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
