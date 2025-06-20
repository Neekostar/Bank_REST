package org.example.neekostar.bank.service;

import java.util.List;
import java.util.UUID;
import org.example.neekostar.bank.dto.UserResponseDto;
import org.example.neekostar.bank.dto.UserUpdateDto;
import org.example.neekostar.bank.entity.User;

public interface UserService {

    User getByEmail(String email);

    UserResponseDto getById(UUID userId);

    UserResponseDto updateUser(UUID userId, UserUpdateDto userUpdateDto);

    List<UserResponseDto> getAllUsers();

    void deleteUser(UUID userId);
}
