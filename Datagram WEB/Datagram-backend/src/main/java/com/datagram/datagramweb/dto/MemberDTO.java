package com.datagram.datagramweb.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MemberDTO {

    @NonNull
    private String email;

    @NonNull
    private String senha;

}
