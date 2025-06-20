package org.example.neekostar.bank.controller;

import java.util.List;
import java.util.UUID;
import org.example.neekostar.bank.dto.CardBlockRequestDto;
import org.example.neekostar.bank.dto.CardBlockResponseDto;
import org.springframework.http.ResponseEntity;

public interface CardBlockRequestController {

    ResponseEntity<Void> submit(CardBlockRequestDto request);

    ResponseEntity<List<CardBlockResponseDto>> pending();

    ResponseEntity<Void> approve(UUID requestId);

    ResponseEntity<Void> reject(UUID requestId);
}
