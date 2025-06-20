package org.example.neekostar.bank.service.impl;

import java.util.List;
import java.util.UUID;
import org.example.neekostar.bank.dto.CardBlockRequestDto;
import org.example.neekostar.bank.dto.CardBlockResponseDto;
import org.example.neekostar.bank.entity.Card;
import org.example.neekostar.bank.entity.CardBlockRequest;
import org.example.neekostar.bank.entity.CardStatus;
import org.example.neekostar.bank.entity.RequestStatus;
import org.example.neekostar.bank.entity.User;
import org.example.neekostar.bank.exception.InvalidArgumentException;
import org.example.neekostar.bank.mapper.CardBlockRequestMapper;
import org.example.neekostar.bank.repository.CardBlockRequestRepository;
import org.example.neekostar.bank.repository.CardRepository;
import org.example.neekostar.bank.service.CardBlockRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CardBlockRequestServiceImpl implements CardBlockRequestService {

    private final CardRepository cardRepository;
    private final CardBlockRequestRepository requestRepository;
    private final CardBlockRequestMapper requestMapper;

    @Autowired
    public CardBlockRequestServiceImpl(CardRepository cardRepository,
                                       CardBlockRequestRepository requestRepository,
                                       CardBlockRequestMapper requestMapper) {
        this.cardRepository = cardRepository;
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
    }

    @Override
    @Transactional
    public void submitRequest(UUID userId, CardBlockRequestDto request) {
        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new InvalidArgumentException("Card not found"));

        if (!card.getOwner().getId().equals(userId)) {
            throw new InvalidArgumentException("You do not own this card");
        }

        User requester = card.getOwner();

        CardBlockRequest cardBlockRequest = new CardBlockRequest();
        cardBlockRequest.setCard(card);
        cardBlockRequest.setReason(request.getReason());
        cardBlockRequest.setStatus(RequestStatus.PENDING);
        cardBlockRequest.setRequester(requester);

        requestRepository.save(cardBlockRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardBlockResponseDto> getPendingRequests() {
        return requestRepository.findByStatus(RequestStatus.PENDING)
                .stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void approveRequest(UUID requestId) {
        CardBlockRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new InvalidArgumentException("Request not found"));

        request.setStatus(RequestStatus.APPROVED);
        request.getCard().setStatus(CardStatus.BLOCKED);

        requestRepository.save(request);
    }

    @Override
    @Transactional
    public void rejectRequest(UUID requestId) {
        CardBlockRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new InvalidArgumentException("Request not found"));

        request.setStatus(RequestStatus.REJECTED);
        requestRepository.save(request);
    }
}
