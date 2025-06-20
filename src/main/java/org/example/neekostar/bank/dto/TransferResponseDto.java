package org.example.neekostar.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class TransferResponseDto {

    private UUID id;

    private String fromCardNumber;

    private String toCardNumber;

    private BigDecimal amount;

    private LocalDateTime timestamp;
}
