package org.example.neekostar.bank.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.example.neekostar.bank.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuthUtils {

    private static JwtTokenProvider staticJwtTokenProvider;

    @Autowired
    public AuthUtils(JwtTokenProvider jwtTokenProvider) {
        AuthUtils.staticJwtTokenProvider = jwtTokenProvider;
    }

    public static UUID getCurrentUserId() {
        String token = extractTokenFromHeader();
        return staticJwtTokenProvider.getUserId(token);
    }

    private static String extractTokenFromHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("Request not found");
        }

        HttpServletRequest request = attributes.getRequest();
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        return bearerToken.substring(7);
    }
}
