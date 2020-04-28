package com.example.datagram.model;

import java.util.List;

public class Pesquisador extends Usuario {

    private String dataInicioTrabalho;
    private String estado;
    private String instituicao;
    private String formacao;
    private String linkCv;
    private String municipio;
    private List<String> topicosInteresse;


    public Pesquisador() {

    }


    public Pesquisador(String nome, String Senha, String email, String dataNasc, String dataInicioTrabalho, String estado, String Municipio, String instituicao, String formacao, String linkCv)
        {
            super(nome, Senha, email, dataNasc, "Pesquisador");
            this.dataInicioTrabalho = dataInicioTrabalho;
            this.estado = estado;
            this.instituicao = instituicao;
            this.formacao = formacao;
            this.municipio = Municipio;
            this.linkCv = linkCv;
        }

        public String getDataInicioTrabalho () {
            return dataInicioTrabalho;
        }

        public void setDataInicioTrabalho (String dataInicioTrabalho){
            this.dataInicioTrabalho = dataInicioTrabalho;
        }

        public String getEstado () {
            return estado;
        }

        public void setEstado (String estado){
            this.estado = estado;
        }

        public String getMunicipio() {
            return municipio;
        }

        public void setMunicipio(String municipio) {
            this.municipio = municipio;
        }

        public String getInstituicao () {
            return instituicao;
        }

        public void setInstituicao (String instituicao){
            this.instituicao = instituicao;
        }

        public String getFormacao () {
            return formacao;
        }

        public void setFormacao (String formacao){
            this.formacao = formacao;
        }

        public String getLinkCv () {
            return linkCv;
        }

        public void setLinkCv (String linkCv){
            this.linkCv = linkCv;
        }


    }