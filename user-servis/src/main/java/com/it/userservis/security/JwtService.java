package com.it.userservis.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.JwtException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY = "questa-chiave-segreta-deve-avere-almeno-32-caratteri";

    private static final long EXPIRATION_TIME = 1000 * 60 * 60;

    private SecretKey getSigngKey() {
        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generatoreToken(String username) {

        Date now = new Date();
        Date expiration = new Date(
                now.getTime() + EXPIRATION_TIME
        );

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .signWith(getSigngKey())
                .compact();
    }

    //verifica token tramite username
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigngKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    //scadenza token
    public Date extractExpiration(String token) {

        return Jwts.parser()
                .verifyWith(getSigngKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }

    public boolean isTokenExpired(String token){

        return extractExpiration(token)
                .before(new Date());
    }

    public boolean isTokenValid(String token, String username) {

        try {
            String usernameToken = extractUsername(token);

            return usernameToken.equals(username) && !isTokenExpired(token);

        } catch (JwtException | IllegalArgumentException e) {

            return false;
        }

    }
}
