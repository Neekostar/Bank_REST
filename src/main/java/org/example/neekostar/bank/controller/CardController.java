package org.example.neekostar.bank.controller;

import java.util.UUID;
import org.example.neekostar.bank.dto.CardRequestDto;
import org.example.neekostar.bank.dto.CardResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface CardController {

    ResponseEntity<CardResponseDto> createCard(UUID userId, CardRequestDto request);

    ResponseEntity<CardResponseDto> getCard(UUID id);

    ResponseEntity<Page<CardResponseDto>> getCardsForUser(int page, int size);

    ResponseEntity<Page<CardResponseDto>> filterCards(String status, int page, int size);

    ResponseEntity<String> getCardBalance(UUID cardId);

    ResponseEntity<Void> blockCard(UUID cardId);

    ResponseEntity<Void> activateCard(UUID cardId);

    ResponseEntity<Void> deleteCard(UUID cardId);
}
