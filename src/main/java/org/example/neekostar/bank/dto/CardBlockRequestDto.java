package org.example.neekostar.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class CardBlockRequestDto {

    @NotNull
    private UUID cardId;

    @NotBlank
    private String reason;
}
