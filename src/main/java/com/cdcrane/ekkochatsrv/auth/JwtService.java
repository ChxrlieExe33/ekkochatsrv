package com.cdcrane.ekkochatsrv.auth;

import com.cdcrane.ekkochatsrv.auth.dto.AccessJwtData;
import com.cdcrane.ekkochatsrv.auth.dto.RefreshJwtData;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
class JwtService implements JwtUseCase {

    @Value("${jwt.access_jwt_secret}")
    private String accessSecret;

    @Value("${jwt.refresh_jwt_secret}")
    private String refreshSecret;

    private SecretKey accessSecretKey;
    private SecretKey refreshSecretKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.access_expiration}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh_expiration}")
    private long refreshTokenExpirationMs;

    @PostConstruct
    private void initializeSecretKeys() {

        if (accessSecret == null || accessSecret.isEmpty() || refreshSecret == null || refreshSecret.isEmpty()) {
            throw new IllegalStateException("JWT secrets must be set!");
        }

        accessSecretKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        refreshSecretKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));

    }

    /**
     * Create the access JWT for a user, with a short expiry.
     * @param auth The authentication object.
     * @return The JWT and relevant data.
     */
    @Override
    public AccessJwtData createAccessJwt(Authentication auth) {

        Date expiration = new Date(System.currentTimeMillis() + accessTokenExpirationMs);

        String jwt =  Jwts.builder()
                .issuer(issuer)
                .subject("JWT Access token")
                .claim("type", "access")
                .claim("username", auth.getName())
                .claim("authorities", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(accessSecretKey)
                .compact();

        return new AccessJwtData(jwt, auth.getName(), expiration);

    }

    /**
     * Create the refresh JWT for the user, allowing to get new access tokens.
     * @param userId The ID of the user.
     * @return The JWT and relevant data.
     */
    @Override
    public RefreshJwtData createRefreshJwt(UUID userId) {

        Date expiration = new Date(System.currentTimeMillis() + refreshTokenExpirationMs);
        var jti =  UUID.randomUUID(); // Token ID.

        String jwt = Jwts.builder()
                .issuer(issuer)
                .subject("JWT Refresh token")
                .claim("type", "refresh")
                .claim("jti", jti)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(refreshSecretKey)
                .compact();

        return new RefreshJwtData(jwt, userId, expiration, jti);

    }


    /**
     * Verifies the integrity of the access JWT by checking the signature with the secret key.
     * Will fail right awat if someone provides their refresh token, since they use different secrets.
     * @param jwt The JWT string.
     * @return The Claims object with the user information.
     */
    @Override
    public Claims verifyAccessJwt(String jwt) {

        try {

            return Jwts.parser()
                    .verifyWith(accessSecretKey)
                    .build()
                        .parseSignedClaims(jwt)
                        .getPayload();

        } catch (ExpiredJwtException e) {
            throw new BadJwtException("Your authentication has expired, please refresh your access token.");
        } catch (Exception e) {
            throw new BadJwtException("Your authentication token is invalid or has been tampered with, please log in again.");
        }
    }

    /**
     * Verifies the integrity of the refresh JWT by checking the signature with the secret key.
     * Will only work for the refresh tokens.
     * @param jwt The JWT string.
     * @return The Claims object with the refresh information.
     */
    @Override
    public Claims verifyRefreshJwt(String jwt) {

        try {

            return Jwts.parser()
                    .verifyWith(refreshSecretKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            throw new BadJwtException("Your refresh token has expired, please log in again.");
        } catch (Exception e) {
            throw new BadJwtException("Your refresh token is invalid or has been tampered with, please log in again.");
        }
    }
}
