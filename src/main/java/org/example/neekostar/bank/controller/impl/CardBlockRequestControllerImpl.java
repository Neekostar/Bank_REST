package org.example.neekostar.bank.controller.impl;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.example.neekostar.bank.controller.CardBlockRequestController;
import org.example.neekostar.bank.dto.CardBlockRequestDto;
import org.example.neekostar.bank.dto.CardBlockResponseDto;
import org.example.neekostar.bank.service.CardBlockRequestService;
import org.example.neekostar.bank.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/card-block-requests")
public class CardBlockRequestControllerImpl implements CardBlockRequestController {

    private final CardBlockRequestService cardBlockRequestService;

    @Autowired
    public CardBlockRequestControllerImpl(CardBlockRequestService cardBlockRequestService) {
        this.cardBlockRequestService = cardBlockRequestService;
    }

    @Override
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> submit(@Valid @RequestBody CardBlockRequestDto request) {
        cardBlockRequestService.submitRequest(AuthUtils.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardBlockResponseDto>> pending() {
        return ResponseEntity.status(HttpStatus.OK).body(cardBlockRequestService.getPendingRequests());
    }

    @Override
    @PostMapping("/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approve(@PathVariable UUID requestId) {
        cardBlockRequestService.approveRequest(requestId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reject(@PathVariable UUID requestId) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
