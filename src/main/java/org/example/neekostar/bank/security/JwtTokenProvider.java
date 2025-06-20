package org.example.neekostar.bank.security;

import java.util.Date;
import java.util.UUID;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.neekostar.bank.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.access-expiration-ms}")
    private long accessTokenValidityMs;

    @Value("${security.jwt.refresh-expiration-ms}")
    private long refreshTokenValidityMs;

    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenValidityMs);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenValidityMs);
    }

    private String generateToken(User user, long expiration) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public UUID getUserId(String token) {
        return UUID.fromString(getClaims(token).getSubject());
    }

    public boolean validate(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
    }
}
