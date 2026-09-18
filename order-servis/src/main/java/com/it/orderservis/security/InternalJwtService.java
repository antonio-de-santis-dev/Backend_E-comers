package com.it.orderservis.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class InternalJwtService {

    /*
     * Deve essere IDENTICA alla INTERNAL_SECRET_KEY
     * presente nel user-servis.
     */
    private static final String INTERNAL_SECRET_KEY =
            "chiave-interna-service-to-service-almeno-32-caratteri";

    private static final long EXPIRATION_TIME =
            1000 * 60 * 5; // 5 minuti

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                INTERNAL_SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateServiceToken() {

        Date now = new Date();

        Date expiration =
                new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject("order-servis")
                .claim("type", "SERVICE")
                .claim(
                        "role",
                        ServiceRole.SERVICE_ORDER.name()
                )
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }
}