package com.datagram.datagramweb.mapper;

import com.datagram.datagramweb.domain.Member;
import com.datagram.datagramweb.dto.MemberDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MemberMapper {
    MemberMapper INSTANCE = Mappers.getMapper(MemberMapper.class);

    MemberDTO memberToMemberDTO(Member member);

    Member memberDTOToMember(MemberDTO memberDTO);
}
