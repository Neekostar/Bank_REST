package org.example.neekostar.bank.service.impl;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.example.neekostar.bank.dto.CardBlockRequestDto;
import org.example.neekostar.bank.dto.CardRequestDto;
import org.example.neekostar.bank.dto.CardResponseDto;
import org.example.neekostar.bank.entity.Card;
import org.example.neekostar.bank.entity.CardStatus;
import org.example.neekostar.bank.entity.User;
import org.example.neekostar.bank.mapper.CardMapper;
import org.example.neekostar.bank.repository.CardRepository;
import org.example.neekostar.bank.repository.UserRepository;
import org.example.neekostar.bank.service.CardBlockRequestService;
import org.example.neekostar.bank.service.CardService;
import org.example.neekostar.bank.util.AuthUtils;
import org.example.neekostar.bank.util.CardEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CardServiceImpl implements CardService {

    private final CardBlockRequestService cardBlockRequestService;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final CardEncryptor cardEncryptor;

    @Autowired
    public CardServiceImpl(CardBlockRequestService cardBlockRequestService,
                           CardRepository cardRepository,
                           UserRepository userRepository,
                           CardMapper cardMapper,
                           CardEncryptor cardEncryptor) {
        this.cardBlockRequestService = cardBlockRequestService;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cardMapper = cardMapper;
        this.cardEncryptor = cardEncryptor;
    }

    @Override
    @Transactional
    public CardResponseDto createCard(UUID userId, CardRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        String generatedCardNumber = generateRandomCardNumber();

        Card card = new Card();
        card.setEncryptedNumber(cardEncryptor.encrypt(generatedCardNumber));
        card.setOwner(user);
        card.setExpiryDate(request.getExpiryDate() != null ? request.getExpiryDate() : LocalDate.now().plusYears(5));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(request.getBalance());

        Card saved = cardRepository.save(card);
        CardResponseDto dto = cardMapper.toDto(saved);
        dto.setMaskedNumber(cardEncryptor.maskCardNumber(generatedCardNumber));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public CardResponseDto getCardById(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found with ID: " + cardId));
        CardResponseDto dto = cardMapper.toDto(card);
        String decrypted = cardEncryptor.decrypt(card.getEncryptedNumber());
        dto.setMaskedNumber(cardEncryptor.maskCardNumber(decrypted));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponseDto> getAllCards(int page, int size, Pageable pageable) {
        Page<Card> cards = cardRepository.findAll(pageable);
        List<CardResponseDto> responses = cards.stream()
                .map(card -> {
                    CardResponseDto dto = cardMapper.toDto(card);
                    String decrypted = cardEncryptor.decrypt(card.getEncryptedNumber());
                    dto.setMaskedNumber(cardEncryptor.maskCardNumber(decrypted));
                    return dto;
                })
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, cards.getTotalElements());
    }


    @Override
    @Transactional(readOnly = true)
    public Page<CardResponseDto> getCardsForCurrentUser(UUID userId, int page, int size, Pageable pageable) {
        Page<Card> pageResult = cardRepository.findByOwnerId(userId, pageable);
        List<CardResponseDto> responses = pageResult.stream()
                .map(card -> {
                    CardResponseDto dto = cardMapper.toDto(card);
                    String decrypted = CardEncryptor.decrypt(card.getEncryptedNumber());
                    dto.setMaskedNumber(CardEncryptor.maskCardNumber(decrypted));
                    return dto;
                })
                .toList();
        return new PageImpl<>(responses, pageable, pageResult.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponseDto> filterCards(UUID userId, int page, int size, String status, Pageable pageable) {
        CardStatus cardStatus = CardStatus.valueOf(status.toUpperCase());
        Page<Card> cards = cardRepository.findByOwner_IdAndStatus(userId, cardStatus, pageable);
        List<CardResponseDto> responses = cards.stream()
                .map(card -> {
                    CardResponseDto dto = cardMapper.toDto(card);
                    String decrypted = CardEncryptor.decrypt(card.getEncryptedNumber());
                    dto.setMaskedNumber(CardEncryptor.maskCardNumber(decrypted));
                    return dto;
                })
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, cards.getTotalElements());
    }

    @Override
    @Transactional
    public void requestBlock(UUID cardId, UUID requesterId) {
        CardBlockRequestDto dto = new CardBlockRequestDto();
        dto.setCardId(cardId);
        dto.setReason("User requested block");

        cardBlockRequestService.submitRequest(requesterId, dto);
    }

    @Override
    @Transactional
    public void blockCard(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found with ID: " + cardId));

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void activateCard(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found with ID: " + cardId));

        if (card.getStatus() != CardStatus.BLOCKED) {
            throw new IllegalArgumentException("Card is not blocked and cannot be activated");
        }

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void deleteCard(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found with ID: " + cardId));

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Active card cannot be deleted. Please block it first.");
        }

        cardRepository.delete(card);
    }

    private String generateRandomCardNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
