package org.example.neekostar.bank.controller.impl;

import java.util.List;
import java.util.UUID;
import org.example.neekostar.bank.controller.TransferController;
import org.example.neekostar.bank.dto.TransferRequestDto;
import org.example.neekostar.bank.dto.TransferResponseDto;
import org.example.neekostar.bank.service.TransferService;
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
@RequestMapping("/api/transfers")
public class TransferControllerImpl implements TransferController {

    private final TransferService transferService;

    @Autowired
    public TransferControllerImpl(TransferService transferService) {
        this.transferService = transferService;
    }

    @Override
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransferResponseDto> transfer(@RequestBody TransferRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transferService.createTransfer(AuthUtils.getCurrentUserId(), request));
    }

    @Override
    @GetMapping("/history/{cardId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<TransferResponseDto>> getTransferHistory(@PathVariable UUID cardId) {
        return ResponseEntity.status(HttpStatus.OK).body(transferService.getTransferHistory(cardId));
    }
}
