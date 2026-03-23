package com.dev.thesis_management.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationDecoder implements JwtDecoder {

    JwtTokenProvider jwtTokenProvider;

    @Override
    public Jwt decode(String token) {

        try {
            Claims claims = jwtTokenProvider.parseAccessToken(token);

            Long iatSeconds = claims.get("iat", Long.class);
            Long expSeconds = claims.get("exp", Long.class);

            Instant issuedAt = iatSeconds != null
                    ? Instant.ofEpochSecond(iatSeconds)
                    : null;

            Instant expiresAt = expSeconds != null
                    ? Instant.ofEpochSecond(expSeconds)
                    : null;

            return Jwt.withTokenValue(token)
                    .subject(claims.getSubject())
                    .issuedAt(issuedAt)
                    .expiresAt(expiresAt)
                    .headers(h -> h.put("alg", "HS256"))
                    .claims(c -> claims.forEach((key, value) -> {
                        if (!"iat".equals(key)
                                && !"exp".equals(key)
                                && !"nbf".equals(key)) {
                            c.put(key, value);
                        }
                    }))
                    .build();

        }
        catch (ExpiredJwtException e) {
            throw oauth2Error("invalid_token", "JWT token has expired", e);
        }
        catch (SignatureException e) {
            throw oauth2Error("invalid_token", "Invalid JWT signature", e);
        }
        catch (MalformedJwtException | UnsupportedJwtException e) {
            throw oauth2Error("invalid_token", "Invalid JWT format", e);
        }
        catch (Exception e) {
            throw oauth2Error("invalid_token", "Invalid JWT token", e);
        }
    }

    private OAuth2AuthenticationException oauth2Error(
            String code,
            String message,
            Exception cause
    ) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(code, message, null),
                cause
        );
    }
}
