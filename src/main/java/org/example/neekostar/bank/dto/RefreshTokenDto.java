package org.example.neekostar.bank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenDto {

    @NotBlank
    private String refreshToken;
}
