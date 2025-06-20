package org.example.neekostar.bank.controller;

import jakarta.validation.Valid;
import org.example.neekostar.bank.dto.JwtResponseDto;
import org.example.neekostar.bank.dto.RefreshTokenDto;
import org.example.neekostar.bank.dto.UserLoginDto;
import org.example.neekostar.bank.dto.UserRegisterDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthController {

    ResponseEntity<Void> register(@Valid @RequestBody UserRegisterDto request);

    ResponseEntity<JwtResponseDto> login(@Valid @RequestBody UserLoginDto request);

    ResponseEntity<JwtResponseDto> refresh(@Valid @RequestBody RefreshTokenDto request);
}
