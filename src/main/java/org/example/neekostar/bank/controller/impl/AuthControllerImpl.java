package org.example.neekostar.bank.controller.impl;

import jakarta.validation.Valid;
import org.example.neekostar.bank.controller.AuthController;
import org.example.neekostar.bank.dto.JwtResponseDto;
import org.example.neekostar.bank.dto.RefreshTokenDto;
import org.example.neekostar.bank.dto.UserLoginDto;
import org.example.neekostar.bank.dto.UserRegisterDto;
import org.example.neekostar.bank.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthControllerImpl implements AuthController {

    private final AuthService authService;

    @Autowired
    public AuthControllerImpl(AuthService authService) {
        this.authService = authService;
    }

    @Override
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody UserRegisterDto request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody UserLoginDto request) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(request));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponseDto> refresh(@Valid @RequestBody RefreshTokenDto request) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.refreshToken(request.getRefreshToken()));
    }
}
