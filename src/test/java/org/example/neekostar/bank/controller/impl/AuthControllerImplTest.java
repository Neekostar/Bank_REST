package org.example.neekostar.bank.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.neekostar.bank.dto.JwtResponseDto;
import org.example.neekostar.bank.dto.RefreshTokenDto;
import org.example.neekostar.bank.dto.UserLoginDto;
import org.example.neekostar.bank.dto.UserRegisterDto;
import org.example.neekostar.bank.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerImplTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    private AuthControllerImpl authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authController = new AuthControllerImpl(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void register_ShouldReturnCreatedStatus() throws Exception {
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setFirstName("Ivan");
        registerDto.setLastName("Ivanov");
        registerDto.setEmail("ivan@example.com");
        registerDto.setPassword("password123");

        doNothing().when(authService).register(any(UserRegisterDto.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void login_ShouldReturnJwtResponse() throws Exception {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail("ivan@example.com");
        loginDto.setPassword("password123");

        JwtResponseDto jwtResponse = new JwtResponseDto();
        jwtResponse.setAccessToken("access-token");
        jwtResponse.setRefreshToken("refresh-token");

        when(authService.login(any(UserLoginDto.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void refresh_ShouldReturnJwtResponse() throws Exception {
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
        refreshTokenDto.setRefreshToken("refresh-token");

        JwtResponseDto jwtResponse = new JwtResponseDto();
        jwtResponse.setAccessToken("new-access-token");
        jwtResponse.setRefreshToken("new-refresh-token");

        when(authService.refreshToken("refresh-token")).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }
}
