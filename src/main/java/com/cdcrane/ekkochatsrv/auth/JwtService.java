package com.cdcrane.ekkochatsrv.auth;

import com.cdcrane.ekkochatsrv.auth.dto.JwtData;
import com.cdcrane.ekkochatsrv.auth.exception.BadJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Service
class JwtService implements JwtUseCase {

    @Value("${jwt.secret}")
    private String secret;
    private SecretKey secretKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiration}")
    private long expirationTimeInMs;

    @PostConstruct
    private void initializeSecretKey() {

        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("JWT secret must be set!");
        }

        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    }

    @Override
    public JwtData createJwt(Authentication auth) {

        Date expiration = new Date(System.currentTimeMillis() + expirationTimeInMs);

        String jwt =  Jwts.builder()
                .issuer(issuer)
                .subject("JWT Token")
                .claim("username", auth.getName())
                .claim("authorities", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(secretKey)
                .compact();

        return new JwtData(jwt, auth.getName(), expiration);

    }

    @Override
    public Claims verifyJwt(String jwt) {

        try {

            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                        .parseSignedClaims(jwt)
                        .getPayload();

        } catch (ExpiredJwtException e) {
            throw new BadJwtException("Your authentication has expired, please log in again.");
        } catch (Exception e) {
            throw new BadJwtException("Your authentication token is invalid or has been tampered with, please log in again.");
        }
    }
}
