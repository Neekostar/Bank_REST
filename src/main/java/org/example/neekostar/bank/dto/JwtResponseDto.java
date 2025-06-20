package org.example.neekostar.bank.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class JwtResponseDto {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
}
