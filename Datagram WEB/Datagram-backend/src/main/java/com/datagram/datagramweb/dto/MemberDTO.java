package com.datagram.datagramweb.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
public class MemberDTO {

    @Email
    private String email;

    @NotBlank
    private String senha;

}
