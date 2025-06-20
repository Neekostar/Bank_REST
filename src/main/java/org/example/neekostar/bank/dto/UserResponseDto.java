package org.example.neekostar.bank.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class UserResponseDto {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private String role;
}
