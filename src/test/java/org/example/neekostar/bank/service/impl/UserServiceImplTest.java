package org.example.neekostar.bank.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.neekostar.bank.dto.UserResponseDto;
import org.example.neekostar.bank.dto.UserUpdateDto;
import org.example.neekostar.bank.entity.User;
import org.example.neekostar.bank.mapper.UserMapper;
import org.example.neekostar.bank.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getByEmail_success() {
        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User result = userService.getByEmail("test@example.com");

        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getByEmail_userNotFound() {
        when(userRepository.findByEmail("none@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getByEmail("none@example.com"));
    }

    @Test
    void getById_success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        UserResponseDto dto = new UserResponseDto();
        dto.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        UserResponseDto result = userService.getById(userId);

        assertEquals(userId, result.getId());
    }

    @Test
    void getById_userNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getById(userId));
    }

    @Test
    void updateUser_success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setFirstName("Old");
        user.setLastName("Name");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setFirstName("New");
        updateDto.setLastName("User");

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(userId);
        responseDto.setFirstName("New");
        responseDto.setLastName("User");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.updateUser(userId, updateDto);

        assertEquals("New", result.getFirstName());
        assertEquals("User", result.getLastName());
    }

    @Test
    void updateUser_userNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(userId, new UserUpdateDto()));
    }

    @Test
    void getAllUsers_success() {
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        User user2 = new User();
        user2.setId(UUID.randomUUID());

        List<User> userList = List.of(user1, user2);

        UserResponseDto dto1 = new UserResponseDto();
        dto1.setId(user1.getId());
        UserResponseDto dto2 = new UserResponseDto();
        dto2.setId(user2.getId());

        when(userRepository.findAll()).thenReturn(userList);
        when(userMapper.toDto(user1)).thenReturn(dto1);
        when(userMapper.toDto(user2)).thenReturn(dto2);

        List<UserResponseDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void deleteUser_success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_userNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(userId));
    }

}
