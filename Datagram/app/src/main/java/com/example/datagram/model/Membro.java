package com.example.datagram.model;

public class Membro extends Usuario {

    private String escolaridade;

    private String CPF;

    public Membro() {
    }

    public Membro(String nome, String Senha, String email, String dataNasc, String escolaridade, String CPF) {
        super(nome, Senha, email, dataNasc, "Membro");
        this.escolaridade = escolaridade;
        this.CPF = CPF;
    }

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
