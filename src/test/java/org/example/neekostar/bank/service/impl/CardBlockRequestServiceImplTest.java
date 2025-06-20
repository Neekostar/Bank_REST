package org.example.neekostar.bank.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.example.neekostar.bank.dto.CardBlockRequestDto;
import org.example.neekostar.bank.dto.CardBlockResponseDto;
import org.example.neekostar.bank.entity.Card;
import org.example.neekostar.bank.entity.CardBlockRequest;
import org.example.neekostar.bank.entity.CardStatus;
import org.example.neekostar.bank.entity.RequestStatus;
import org.example.neekostar.bank.exception.InvalidArgumentException;
import org.example.neekostar.bank.mapper.CardBlockRequestMapper;
import org.example.neekostar.bank.repository.CardBlockRequestRepository;
import org.example.neekostar.bank.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

public class CardBlockRequestServiceImplTest {

    private CardBlockRequestServiceImpl service;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardBlockRequestRepository requestRepository;

    @Mock
    private CardBlockRequestMapper requestMapper;

    private UUID userId;
    private UUID cardId;
    private UUID requestId;

    private Card card;
    private CardBlockRequest requestEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        service = new CardBlockRequestServiceImpl(cardRepository, requestRepository, requestMapper);

        userId = UUID.randomUUID();
        cardId = UUID.randomUUID();
        requestId = UUID.randomUUID();

        card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);
        card.setOwner(new org.example.neekostar.bank.entity.User());
        card.getOwner().setId(userId);

        requestEntity = new CardBlockRequest();
        requestEntity.setId(requestId);
        requestEntity.setCard(card);
        requestEntity.setReason("Reason");
        requestEntity.setStatus(RequestStatus.PENDING);
    }

    @Test
    void submitRequest_ShouldSaveRequest_WhenUserIsOwner() {
        CardBlockRequestDto dto = new CardBlockRequestDto();
        dto.setCardId(cardId);
        dto.setReason("Need to block");

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(requestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.submitRequest(userId, dto);

        ArgumentCaptor<CardBlockRequest> captor = ArgumentCaptor.forClass(CardBlockRequest.class);
        verify(requestRepository).save(captor.capture());

        CardBlockRequest saved = captor.getValue();
        assertThat(saved.getCard()).isEqualTo(card);
        assertThat(saved.getReason()).isEqualTo("Need to block");
        assertThat(saved.getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    void submitRequest_ShouldThrow_WhenCardNotFound() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        CardBlockRequestDto dto = new CardBlockRequestDto();
        dto.setCardId(cardId);
        dto.setReason("Reason");

        assertThatThrownBy(() -> service.submitRequest(userId, dto))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Card not found");
    }

    @Test
    void submitRequest_ShouldThrow_WhenUserIsNotOwner() {
        Card otherCard = new Card();
        otherCard.setOwner(new org.example.neekostar.bank.entity.User());
        otherCard.getOwner().setId(UUID.randomUUID());

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(otherCard));

        CardBlockRequestDto dto = new CardBlockRequestDto();
        dto.setCardId(cardId);
        dto.setReason("Reason");

        assertThatThrownBy(() -> service.submitRequest(userId, dto))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("You do not own this card");
    }

    @Test
    void getPendingRequests_ShouldReturnMappedDtos() {
        List<CardBlockRequest> requests = List.of(requestEntity);
        CardBlockResponseDto dto = new CardBlockResponseDto();

        when(requestRepository.findByStatus(RequestStatus.PENDING)).thenReturn(requests);
        when(requestMapper.toDto(requestEntity)).thenReturn(dto);

        List<CardBlockResponseDto> result = service.getPendingRequests();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(dto);
    }

    @Test
    void approveRequest_ShouldSetStatusApprovedAndBlockCard() {
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(requestEntity));
        when(requestRepository.save(requestEntity)).thenReturn(requestEntity);

        service.approveRequest(requestId);

        assertThat(requestEntity.getStatus()).isEqualTo(RequestStatus.APPROVED);
        assertThat(requestEntity.getCard().getStatus()).isEqualTo(CardStatus.BLOCKED);
        verify(requestRepository).save(requestEntity);
    }

    @Test
    void approveRequest_ShouldThrow_WhenRequestNotFound() {
        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approveRequest(requestId))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Request not found");
    }

    @Test
    void rejectRequest_ShouldSetStatusRejected() {
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(requestEntity));
        when(requestRepository.save(requestEntity)).thenReturn(requestEntity);

        service.rejectRequest(requestId);

        assertThat(requestEntity.getStatus()).isEqualTo(RequestStatus.REJECTED);
        verify(requestRepository).save(requestEntity);
    }

    @Test
    void rejectRequest_ShouldThrow_WhenRequestNotFound() {
        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rejectRequest(requestId))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Request not found");
    }
}
