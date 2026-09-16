package com.it.userservis.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY = "questa-chiave-segreta-deve-avere-almeno-32-caratteri";

    private static final long EXPIRATION_TIME = 1000 * 60 * 60;

    private SecretKey getSigngKey(){
        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generatoreToken(String username){

        Date now = new Date();
        Date expiration =  new Date(
                now.getTime() + EXPIRATION_TIME
        );

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .signWith(getSigngKey())
                .compact();
    }
}
