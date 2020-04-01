package com.example.datagram.model;

public class Membro extends Usuario {

    private String escolaridade;

    private String senha;

    public Membro() {
    }

    //CadastroBase
    public Membro(String nome,String CPF, String email, String dataNasc){
        super(nome, CPF, email, dataNasc);
    }
    //Cadastro especifico
    public Membro(String nome, String CPF, String email, String dataNasc, String escolaridade, String senha) {
        super(nome, CPF, email, dataNasc);
        this.escolaridade = escolaridade;
        this.senha = senha;
    }

    public String getEscolaridade() {
        return escolaridade;
    }

    public void setEscolaridade(String escolaridade) {
        this.escolaridade = escolaridade;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
