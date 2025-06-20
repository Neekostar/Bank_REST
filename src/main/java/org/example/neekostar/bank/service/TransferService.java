package org.example.neekostar.bank.service;

import java.util.List;
import java.util.UUID;
import org.example.neekostar.bank.dto.TransferRequestDto;
import org.example.neekostar.bank.dto.TransferResponseDto;

public interface TransferService {

    TransferResponseDto createTransfer(UUID userId, TransferRequestDto request);

    List<TransferResponseDto> getTransferHistory(UUID cardId);
}
