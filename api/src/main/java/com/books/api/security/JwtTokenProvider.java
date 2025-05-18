package com.books.api.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    @Value("${security.expiration}")
    private long validityInMilliseconds; // 1 hour

    /*
     * Generate a JWT token and set its expiration time.
     *
     * @return The generated JWT token.
     */
    public String createToken() {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey)
                .compact();
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
}