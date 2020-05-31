package com.datagram.datagramweb.Models;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Postagem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "Autor_id")
    private Usuario autor;

    @ElementCollection
    @CollectionTable(name = "postagem_comentario", joinColumns = @JoinColumn(name = "postagem_id"))
    private List<Comentario> comentarios = new ArrayList<>();

    private String titulo;
    private String subtitulo;
    private String conteudo;
    private Date date;
    private Integer curtida;

    public Postagem(){
    }

    public Postagem(Integer id, Usuario autor, String titulo, String subtitulo, String conteudo, Date date, Integer curtida) {
        this.id = id;
        this.autor = autor;
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.conteudo = conteudo;
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
        return autor;
    }

    public void setUsuario(Usuario usuario) {
        this.autor = usuario;
    }

    
    public String getTexto() {
        return conteudo;
    }

    public void setTexto(String texto) {
        this.conteudo = texto;
    }

    public List<Comentario> getComentario() {
        return comentarios;
    }

    public void setComentario(List<Comentario> comentario) {
        this.comentarios = comentario;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public void setSubtitulo(String subtitulo) {
        this.subtitulo = subtitulo;
    }
}
