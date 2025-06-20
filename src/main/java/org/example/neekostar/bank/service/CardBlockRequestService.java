package org.example.neekostar.bank.service;

import java.util.List;
import java.util.UUID;
import org.example.neekostar.bank.dto.CardBlockRequestDto;
import org.example.neekostar.bank.dto.CardBlockResponseDto;

public interface CardBlockRequestService {

    void submitRequest(UUID userId, CardBlockRequestDto request);

    List<CardBlockResponseDto> getPendingRequests();

    void approveRequest(UUID requestId);

    void rejectRequest(UUID requestId);
}
