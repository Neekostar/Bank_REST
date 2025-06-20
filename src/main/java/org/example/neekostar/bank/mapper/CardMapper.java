package org.example.neekostar.bank.mapper;

import org.example.neekostar.bank.dto.CardResponseDto;
import org.example.neekostar.bank.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "ownerEmail", source = "owner.email")
    CardResponseDto toDto(Card card);
}
