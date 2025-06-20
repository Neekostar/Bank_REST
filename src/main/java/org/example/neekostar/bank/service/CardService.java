package org.example.neekostar.bank.service;

import java.util.UUID;
import org.example.neekostar.bank.dto.CardRequestDto;
import org.example.neekostar.bank.dto.CardResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {

    CardResponseDto createCard(UUID userId, CardRequestDto request);

    CardResponseDto getCardById(UUID cardId);

    Page<CardResponseDto> getAllCards(int page, int size, Pageable pageable);

    Page<CardResponseDto> getCardsForCurrentUser(UUID userId, int page, int size, Pageable pageable);

    Page<CardResponseDto> filterCards(UUID userId, int page, int size, String status, Pageable pageable);

    void requestBlock(UUID cardId, UUID requesterId);

    void blockCard(UUID cardId);

    void activateCard(UUID cardId);

    void deleteCard(UUID cardId);
}
