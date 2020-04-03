package com.example.datagram.model;

public class Membro extends Usuario {

    private String escolaridade;

    private String CPF;

    public Membro() {
    }

    //CadastroBase
    public Membro(String nome,String Senha, String email, String dataNasc){
        super(nome, Senha, email, dataNasc);
    }
    //Cadastro especifico

    public String getEscolaridade() {
        return escolaridade;
    }

    public void setEscolaridade(String escolaridade) {
        this.escolaridade = escolaridade;
    }

    public String getCPF() {
        return CPF;
    }

    public void setCPF(String CPF) {
        this.CPF = CPF;
    }
}
