package org.example.neekostar.bank.service.impl;

import java.util.Optional;
import java.util.UUID;
import org.example.neekostar.bank.dto.JwtResponseDto;
import org.example.neekostar.bank.dto.UserLoginDto;
import org.example.neekostar.bank.dto.UserRegisterDto;
import org.example.neekostar.bank.entity.Role;
import org.example.neekostar.bank.entity.User;
import org.example.neekostar.bank.exception.DuplicateResourceException;
import org.example.neekostar.bank.exception.UnauthorizedException;
import org.example.neekostar.bank.repository.UserRepository;
import org.example.neekostar.bank.security.JwtTokenProvider;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private final String email = "test@mail.com";
    private final String password = "password";
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);
    }

    @Test
    void login_Success() {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail(email);
        loginDto.setPassword(password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn("refresh-token");

        JwtResponseDto response = authService.login(loginDto);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail(email);
        loginDto.setPassword(password);

        assertThrows(UnauthorizedException.class, () -> authService.login(loginDto));
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail(email);
        loginDto.setPassword(password);

        assertThrows(UnauthorizedException.class, () -> authService.login(loginDto));
    }

    @Test
    void refreshToken_Success() {
        String refreshToken = "valid-token";

        when(jwtTokenProvider.validate(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserId(refreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("new-access");
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn("new-refresh");

        JwtResponseDto result = authService.refreshToken(refreshToken);

        assertEquals("new-access", result.getAccessToken());
        assertEquals("new-refresh", result.getRefreshToken());
    }

    @Test
    void refreshToken_Invalid_ThrowsException() {
        when(jwtTokenProvider.validate("bad-token")).thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> authService.refreshToken("bad-token"));
    }

    @Test
    void register_Success() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setFirstName("Test");
        dto.setLastName("User");

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        authService.register(dto);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ExistingEmail_ThrowsException() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail(email);

        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(dto));
    }

}
