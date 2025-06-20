package org.example.neekostar.bank.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class CardBlockResponseDto {

    private UUID id;
    private UUID cardId;
    private UUID requesterId;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
}
