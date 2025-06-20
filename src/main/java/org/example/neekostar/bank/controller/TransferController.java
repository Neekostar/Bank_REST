package org.example.neekostar.bank.controller;

import java.util.List;
import java.util.UUID;
import org.example.neekostar.bank.dto.TransferRequestDto;
import org.example.neekostar.bank.dto.TransferResponseDto;
import org.springframework.http.ResponseEntity;

public interface TransferController {

    ResponseEntity<TransferResponseDto> transfer(TransferRequestDto request);

    ResponseEntity<List<TransferResponseDto>> getTransferHistory(UUID cardId);
}
