package com.example.datagram.model;

import android.os.Build;


import com.example.datagram.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//implementando a interface Serializable é possivel enviar um obj(usuario) de uma view para outra view
public  class Usuario implements Serializable {

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
        setNome(nome);//iremos sempre criar usuario com o nome em caixa alta, evitando problemas de busca
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

    public void atualizar(){
        //entramos no database
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        //e depois na arvore de usuarios, e filtramos apenas 1 user pelo id
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(getId());
        Map<String,Object> valoresUsuarios = converterParaMap();
        usuarioRef.updateChildren(valoresUsuarios);
    }

    public Map<String,Object> converterParaMap(){
        HashMap<String,Object> usuarioMap = new HashMap<>();
        usuarioMap.put("email",getEmail());
        usuarioMap.put("nome",getNome());
        usuarioMap.put("id",getId());
        usuarioMap.put("caminhoFoto",getCaminhoFoto());
        return usuarioMap;
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
        this.nome = nome.toUpperCase();
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

    public static int getNumeroUsuarios() {
        return numeroUsuarios;
    }
}
