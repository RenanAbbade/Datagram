package com.example.datagram.model;

public class Pesquisador extends Usuario {

    private String dataInicioTrabalho;
    private String estado;
    private String instituicao;
    private String formacao;
    private String linkCv;


    public Pesquisador() {

    }
<<<<<<< Updated upstream
    //CadastroBase
    public Pesquisador(String nome,String Senha, String email, String dataNasc){
        super(nome, Senha, email, dataNasc);
=======

    public Pesquisador(String nome, String Senha, String email, String dataNasc, String dataInicioTrabalho, String estado, String instituicao, String formacao, String linkCv) {
        super(nome, Senha, email, dataNasc,"Pesquisador");
        this.dataInicioTrabalho = dataInicioTrabalho;
        this.estado = estado;
        this.instituicao = instituicao;
        this.formacao = formacao;
        this.linkCv = linkCv;
>>>>>>> Stashed changes
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
        return formacao;
    }

    public void setFormacao(String formacao) {
        this.formacao = formacao;
    }

    public String getLinkCv() {
        return linkCv;
    }

    public void setLinkCv(String linkCv) {
        this.linkCv = linkCv;
    }

<<<<<<< Updated upstream
=======



>>>>>>> Stashed changes
}
