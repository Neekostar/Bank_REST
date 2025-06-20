package org.example.neekostar.bank.service;

import org.example.neekostar.bank.dto.JwtResponseDto;
import org.example.neekostar.bank.dto.UserLoginDto;
import org.example.neekostar.bank.dto.UserRegisterDto;

public interface AuthService {

    JwtResponseDto login(UserLoginDto userLoginDto);

    JwtResponseDto refreshToken(String refreshToken);

    void register(UserRegisterDto userRegisterDto);
}
