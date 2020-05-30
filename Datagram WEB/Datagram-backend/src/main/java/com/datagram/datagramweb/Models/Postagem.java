package com.datagram.datagramweb.Models;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class Postagem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "Usuario_id")
    private Usuario usuario;

    private String conteudo;
    private String comentario;
    private Date date;
    private Integer curtida;

    public Postagem(){
    }

    public Postagem(Integer id, Usuario usuario, String conteudo, String comentario, Date date, Integer curtida) {
        this.id = id;
        this.usuario = usuario;
        this.conteudo = conteudo;
        this.comentario = comentario;
        this.date = date;
        this.curtida = curtida;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTexto() {
        return conteudo;
    }

    public void setTexto(String texto) {
        this.conteudo = texto;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getCurtida() {
        return curtida;
    }

    public void setCurtida(Integer curtida) {
        this.curtida = curtida;
    }
}
