package com.it.userservis.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class InternalJwtService {

    private static final String INTERNAL_SECRET_KEY =
            "chiave-interna-service-to-service-almeno-32-caratteri";

    private static final long EXPIRATION_TIME =
            1000 * 60 * 5; // 5 minuti

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                INTERNAL_SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateServiceToken(ServiceRole serviceRole) {

        Date now = new Date();

        Date expiration =
                new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(serviceRole.name())
                .claim("type", "SERVICE")
                .claim("role", serviceRole.name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractRole(String token) {

        return extractClaims(token)
                .get("role", String.class);
    }

    public String extractType(String token) {

        return extractClaims(token)
                .get("type", String.class);
    }

    public Date extractExpiration(String token) {

        return extractClaims(token)
                .getExpiration();
    }

    public boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }

    public boolean isTokenValid(String token) {

        try {

            Claims claims = extractClaims(token);

            String type =
                    claims.get("type", String.class);

            return "SERVICE".equals(type)
                    && !isTokenExpired(token);

        } catch (JwtException | IllegalArgumentException e) {

            return false;
        }
    }
}