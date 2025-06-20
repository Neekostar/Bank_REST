package org.example.neekostar.bank.mapper;

import org.example.neekostar.bank.dto.CardBlockResponseDto;
import org.example.neekostar.bank.entity.CardBlockRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardBlockRequestMapper {

    @Mapping(target = "cardId", source = "card.id")
    @Mapping(target = "requesterId", source = "requester.id")
    CardBlockResponseDto toDto(CardBlockRequest request);
}
