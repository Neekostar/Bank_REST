package org.example.neekostar.bank.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.example.neekostar.bank.util.CardEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class CardServiceImplTest {

    private CardEncryptor cardEncryptor;

    @Mock
    private CardBlockRequestService cardBlockRequestService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    private CardServiceImpl cardService;

    private UUID userId;
    private UUID cardId;
    private User user;
    private Card card;
    private CardRequestDto cardRequestDto;
    private CardResponseDto cardResponseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cardEncryptor = new CardEncryptor("1234567890123456");
        cardEncryptor.init();

        cardService = new CardServiceImpl(
                cardBlockRequestService,
                cardRepository,
                userRepository,
                cardMapper,
                cardEncryptor
        );

        userId = UUID.randomUUID();
        cardId = UUID.randomUUID();

        user = new User();
        user.setId(userId);

        cardRequestDto = new CardRequestDto();
        cardRequestDto.setBalance(BigDecimal.valueOf(1000L));
        cardRequestDto.setExpiryDate(LocalDate.now().plusYears(3));

        card = new Card();
        card.setId(cardId);
        card.setOwner(user);
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(1000L));
        card.setExpiryDate(cardRequestDto.getExpiryDate());

        cardResponseDto = new CardResponseDto();
        cardResponseDto.setId(cardId);
        cardResponseDto.setBalance(BigDecimal.valueOf(1000L));
        cardResponseDto.setExpiryDate(cardRequestDto.getExpiryDate());
    }

    @Test
    void createCard_ShouldReturnCreatedCardDto() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardResponseDto);

        CardResponseDto result = cardService.createCard(userId, cardRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getMaskedNumber()).startsWith("**** **** **** ");
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_UserNotFound_ShouldThrow() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(userId, cardRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getCardById_ShouldReturnCardDto() {
        String plainCardNumber = "1234567890123456";
        String encryptedNumber = cardEncryptor.encrypt(plainCardNumber);
        card.setEncryptedNumber(encryptedNumber);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardResponseDto);

        CardResponseDto result = cardService.getCardById(cardId);

        assertThat(result).isNotNull();
        assertThat(result.getMaskedNumber()).isEqualTo("**** **** **** 3456");
    }

    @Test
    void getCardById_CardNotFound_ShouldThrow() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardById(cardId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Card not found");
    }

    @Test
    void getAllCards_ShouldReturnPagedDtos() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Card> cards = List.of(card);

        String plainCardNumber = "1234567890123456";
        card.setEncryptedNumber(cardEncryptor.encrypt(plainCardNumber));

        Page<Card> page = new PageImpl<>(cards, pageable, cards.size());

        when(cardRepository.findAll(pageable)).thenReturn(page);
        when(cardMapper.toDto(card)).thenReturn(cardResponseDto);

        Page<CardResponseDto> result = cardService.getAllCards(0, 10, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getMaskedNumber()).isEqualTo("**** **** **** 3456");
    }

    @Test
    void blockCard_ShouldChangeStatusToBlocked() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.blockCard(cardId);

        assertThat(card.getStatus()).isEqualTo(CardStatus.BLOCKED);
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_CardNotFound_ShouldThrow() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.blockCard(cardId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Card not found");
    }

    @Test
    void activateCard_ShouldChangeStatusToActive() {
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.activateCard(cardId);

        assertThat(card.getStatus()).isEqualTo(CardStatus.ACTIVE);
        verify(cardRepository).save(card);
    }

    @Test
    void activateCard_WhenNotBlocked_ShouldThrow() {
        card.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.activateCard(cardId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Card is not blocked");
    }

    @Test
    void deleteCard_ShouldDeleteWhenNotActive() {
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.deleteCard(cardId);

        verify(cardRepository).delete(card);
    }

    @Test
    void deleteCard_ActiveCard_ShouldThrow() {
        card.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.deleteCard(cardId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Active card cannot be deleted");
    }

    @Test
    void requestBlock_ShouldCallBlockRequestService() {
        cardService.requestBlock(cardId, userId);

        ArgumentCaptor<CardBlockRequestDto> captor = ArgumentCaptor.forClass(CardBlockRequestDto.class);
        verify(cardBlockRequestService).submitRequest(eq(userId), captor.capture());

        CardBlockRequestDto dto = captor.getValue();
        assertThat(dto.getCardId()).isEqualTo(cardId);
        assertThat(dto.getReason()).isEqualTo("User requested block");
    }
}
