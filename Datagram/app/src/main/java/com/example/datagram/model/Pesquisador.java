package com.example.datagram.model;

public class Pesquisador extends Usuario {

    private String dataInicioTrabalho;
    private String estado;
    private String instituicao;
    private String Formacao;
    private String linkCv;
    private String senha;

    public Pesquisador() {

    }
    //CadastroBase
    public Pesquisador(String nome,String CPF, String email, String dataNasc){
        super(nome, CPF, email, dataNasc);
    }
    //CadastroEspecifico
    public Pesquisador(String nome, String CPF, String email, String dataNasc, String dataInicioTrabalho, String estado, String instituicao, String formacao, String linkCv, String senha) {
        super(nome, CPF, email, dataNasc);
        this.dataInicioTrabalho = dataInicioTrabalho;
        this.estado = estado;
        this.instituicao = instituicao;
        this.Formacao = formacao;
        this.linkCv = linkCv;
        this.senha = senha;
    }

    public String getDataInicioTrabalho() {
        return dataInicioTrabalho;
    }

    public void setDataInicioTrabalho(String dataInicioTrabalho) {
        this.dataInicioTrabalho = dataInicioTrabalho;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(String instituicao) {
        this.instituicao = instituicao;
    }

    public String getFormacao() {
        return Formacao;
    }

    public void setFormacao(String formacao) {
        Formacao = formacao;
    }

    public String getLinkCv() {
        return linkCv;
    }

    public void setLinkCv(String linkCv) {
        this.linkCv = linkCv;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
