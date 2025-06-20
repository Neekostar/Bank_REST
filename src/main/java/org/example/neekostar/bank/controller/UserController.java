package org.example.neekostar.bank.controller;

import java.util.List;
import java.util.UUID;
import org.example.neekostar.bank.dto.UserResponseDto;
import org.example.neekostar.bank.dto.UserUpdateDto;
import org.springframework.http.ResponseEntity;

public interface UserController {

    ResponseEntity<UserResponseDto> getUserById(UUID userId);

    ResponseEntity<UserResponseDto> updateUser(UUID userId, UserUpdateDto userUpdateDto);

    ResponseEntity<List<UserResponseDto>> getAllUsers();

    ResponseEntity<Void> deleteUser(UUID userId);
}
