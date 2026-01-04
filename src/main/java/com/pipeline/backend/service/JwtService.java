package com.pipeline.backend.service;

import com.pipeline.backend.utils.AesCryptoUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    public String generateSignupToken(String claim) {


        return Jwts.builder()
                .setSubject("signup")
                .claim("uid", claim)
                .setExpiration(Date.from(
                        Instant.now().plus(Duration.ofMinutes(5))
                ))
                .signWith(getSignInKey() ,SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

