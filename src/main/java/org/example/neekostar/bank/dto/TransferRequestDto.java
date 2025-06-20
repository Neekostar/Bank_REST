package org.example.neekostar.bank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class TransferRequestDto {

    @NotNull(message = "Sender card ID is required")
    private UUID fromCardId;

    @NotNull(message = "Recipient card ID is required")
    private UUID toCardId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
    private BigDecimal amount;
}
