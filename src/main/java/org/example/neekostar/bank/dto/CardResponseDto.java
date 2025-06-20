package org.example.neekostar.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class CardResponseDto {

    private UUID id;

    private String maskedNumber;

    private String ownerEmail;

    private LocalDate expiryDate;

    private String status;

    private BigDecimal balance;
}
