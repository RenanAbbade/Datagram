package com.datagram.datagramweb.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class MemberDTO {

    @Email
    private String email;

    @NotBlank
    private String nome;

    @NotBlank
    private String senha;

    @NotBlank
    private String dataNasc;

    @NotBlank
    private String cpf;

    @NotBlank
    private String escolaridade;

    @NotBlank
    private String estado;

    @NotBlank
    private String municipio;

    @NotBlank
    private String instituicao;

    private String linkCv;

    private String tipoUsuario;

}
