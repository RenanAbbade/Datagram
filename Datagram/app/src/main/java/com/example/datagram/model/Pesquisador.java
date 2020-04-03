package com.example.datagram.model;

public class Pesquisador extends Usuario {

    private String dataInicioTrabalho;
    private String estado;
    private String instituicao;
    private String Formacao;
    private String linkCv;


    public Pesquisador() {

    }
    //CadastroBase
    public Pesquisador(String nome,String Senha, String email, String dataNasc){
        super(nome, Senha, email, dataNasc);
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

}
