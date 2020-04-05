package com.example.datagram.model;

import android.os.Build;


import com.example.datagram.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

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

    public void salvar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        //Criação da entidade que irá salvar o usuário no banco.
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(getId());

        usuarioRef.setValue(this);
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
    //Para excluir a senha no momento de salvar no banco de dados - A senha será utilizada somente na autenticação no Firebase
    @Exclude
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
