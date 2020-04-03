package com.example.datagram.model;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Objects;

public  class Usuario {

    private String id;
    private String nome;
    private String Senha;
    private String email;
    private String dataNasc;
    private String caminhoFoto;
    public static int numeroUsuarios;


    public Usuario() {
    }

    public Usuario(String nome, String Senha, String email, String dataNasc){
        this.nome = nome;
        this.Senha = Senha;
        this.email = email;
        this.dataNasc = dataNasc;
        numeroUsuarios++;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSenha() {
        return Senha;
    }

    public void setSenha(String Senha) {
        this.Senha = Senha;
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

}
