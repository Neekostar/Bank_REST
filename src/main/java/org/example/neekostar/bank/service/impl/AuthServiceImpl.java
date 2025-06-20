package org.example.neekostar.bank.service.impl;

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
import org.example.neekostar.bank.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public JwtResponseDto login(UserLoginDto userLoginDto) {
        User user = userRepository.findByEmail(userLoginDto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        JwtResponseDto jwtResponseDto = new JwtResponseDto();
        jwtResponseDto.setAccessToken(accessToken);
        jwtResponseDto.setRefreshToken(refreshToken);

        return jwtResponseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public JwtResponseDto refreshToken(String refreshToken) {
        if( !jwtTokenProvider.validate(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        UUID userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        JwtResponseDto jwtResponseDto = new JwtResponseDto();
        jwtResponseDto.setAccessToken(newAccessToken);
        jwtResponseDto.setRefreshToken(newRefreshToken);

        return jwtResponseDto;
    }

    @Override
    @Transactional
    public void register(UserRegisterDto userRegisterDto) {
        if (userRepository.existsByEmail(userRegisterDto.getEmail())) {
            throw new DuplicateResourceException("User with this email already exists");
        }

        User newUser = new User();
        newUser.setEmail(userRegisterDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));
        newUser.setRole(Role.USER);
        newUser.setFirstName(userRegisterDto.getFirstName());
        newUser.setLastName(userRegisterDto.getLastName());

        userRepository.save(newUser);
    }
}
