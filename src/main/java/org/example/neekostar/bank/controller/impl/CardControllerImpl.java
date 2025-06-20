package org.example.neekostar.bank.controller.impl;

import jakarta.validation.Valid;
import java.util.UUID;
import org.example.neekostar.bank.controller.CardController;
import org.example.neekostar.bank.dto.CardRequestDto;
import org.example.neekostar.bank.dto.CardResponseDto;
import org.example.neekostar.bank.service.CardService;
import org.example.neekostar.bank.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards")
public class CardControllerImpl implements CardController {

    private final CardService cardService;

    @Autowired
    public CardControllerImpl(CardService cardService) {
        this.cardService = cardService;
    }

    @Override
    @PostMapping("/{userId}/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponseDto> createCard(@PathVariable UUID userId,
                                                      @Valid @RequestBody CardRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(userId, request));
    }

    @Override
    @GetMapping("/{cardId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CardResponseDto> getCard(@PathVariable UUID cardId) {
        return ResponseEntity.status(HttpStatus.OK).body(cardService.getCardById(cardId));
    }

    @Override
    @GetMapping("/{cardId}/balance")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> getCardBalance(@PathVariable UUID cardId) {
        CardResponseDto card = cardService.getCardById(cardId);
        return ResponseEntity.ok("Balance: " + card.getBalance() + "$");
    }

    @Override
    @GetMapping()
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<CardResponseDto>> getCardsForUser(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.OK).body(cardService.getCardsForCurrentUser(AuthUtils.getCurrentUserId(), page, size, PageRequest.of(page, size)));
    }

    @Override
    @GetMapping("/filter")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<CardResponseDto>> filterCards(@RequestParam String status,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.OK).body(cardService.filterCards(AuthUtils.getCurrentUserId(), page, size, status, PageRequest.of(page, size)));
    }

    @Override
    @PatchMapping("/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blockCard(@PathVariable UUID cardId) {
        cardService.blockCard(cardId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @PatchMapping("/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateCard(@PathVariable UUID cardId) {
        cardService.activateCard(cardId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @PatchMapping("/{cardId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
