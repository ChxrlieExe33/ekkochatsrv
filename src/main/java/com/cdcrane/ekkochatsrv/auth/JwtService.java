package com.cdcrane.ekkochatsrv.auth;

import com.cdcrane.ekkochatsrv.auth.dto.AccessJwtData;
import com.cdcrane.ekkochatsrv.auth.dto.RefreshJwtData;
import com.cdcrane.ekkochatsrv.auth.dto.TokenPairResponse;
import com.cdcrane.ekkochatsrv.auth.enums.JwtTypes;
import com.cdcrane.ekkochatsrv.auth.enums.NamedJwtClaims;
import com.cdcrane.ekkochatsrv.auth.exceptions.BadJwtException;
import com.cdcrane.ekkochatsrv.auth.exceptions.TokenNotFoundException;
import com.cdcrane.ekkochatsrv.auth.refresh_token.RefreshTokenEntry;
import com.cdcrane.ekkochatsrv.auth.refresh_token.RefreshTokenRepository;
import com.cdcrane.ekkochatsrv.users.ApplicationUser;
import com.cdcrane.ekkochatsrv.users.UserUseCase;
import com.cdcrane.ekkochatsrv.users.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class JwtService implements JwtUseCase {

    private final RefreshTokenRepository refreshTokenRepo;
    private final UserUseCase userService;

    @Value("${jwt.refresh_token_storage_pepper}")
    private String refreshTokenStoragePepper;

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
                .claim(NamedJwtClaims.TYPE.name(), JwtTypes.ACCESS.name())
                .claim(NamedJwtClaims.USERNAME.name(), auth.getName())
                .claim(NamedJwtClaims.AUTHORITIES.name(), auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(accessSecretKey)
                .compact();

        return new AccessJwtData(jwt, auth.getName(), expiration);

    }

    /**
     * Overloaded version of this method for when you don't have an Authentication object.
     * @param username The username for the JWT.
     * @param roles The roles.
     * @return An object with the access token data.
     */
    @Override
    public AccessJwtData createAccessJwt(String username, Set<String> roles) {

        Date expiration = new Date(System.currentTimeMillis() + accessTokenExpirationMs);

        String jwt =  Jwts.builder()
                .issuer(issuer)
                .subject("JWT Access token")
                .claim(NamedJwtClaims.TYPE.name(), JwtTypes.ACCESS.name())
                .claim(NamedJwtClaims.USERNAME.name(), username)
                .claim(NamedJwtClaims.AUTHORITIES.name(), String.join(",", roles))
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(accessSecretKey)
                .compact();

        return new AccessJwtData(jwt, username, expiration);

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
                .claim(NamedJwtClaims.TYPE.name(), JwtTypes.REFRESH.name())
                .claim(NamedJwtClaims.JTI.name(), jti)
                .claim(NamedJwtClaims.USERID.name(), userId)
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
            throw new BadJwtException("Your token is invalid, make sure you are using your access token.");
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

    /**
     * With the refresh token, it checks integrity, expiry and type, also if it's been revoked from the DB.
     * Then retrieves the user object from their ID, and creates a new access token for them.
     * Finally, revokes DB entry for old refresh token and creates one for the new one.
     * @param refreshToken The users current refresh token in string format.
     * @return A pair of new tokens.
     */
    @Override
    @Transactional
    public TokenPairResponse refreshBothTokens(String refreshToken) {

        // Verifies its integrity & expiry (will throw an exception if its expired), then returns the claims
        var refreshClaims = this.verifyRefreshJwt(refreshToken);

        if (!refreshClaims.get(NamedJwtClaims.TYPE.name()).equals(JwtTypes.REFRESH.name())) {
            throw new BadJwtException("You cannot use an access token to refresh.");
        }

        String tokenId = refreshClaims.get(NamedJwtClaims.JTI.name(), String.class);

        if (tokenId == null) {
            throw new BadJwtException("Refresh token was created wrong and does not contain a JTI.");
        }

        UUID jti = UUID.fromString(tokenId);

        // Check to make sure it's not been revoked
        var originalRefreshEntry = refreshTokenRepo.findByJti(jti)
                .orElseThrow(() -> new TokenNotFoundException("Refresh token not found on server, most likely revoked."));

        var userIdString = refreshClaims.get(NamedJwtClaims.USERID.name(), String.class);

        UUID userId = UUID.fromString(userIdString);

        // Need to get user account from the database since the SecurityContext won't be populated.
        ApplicationUser user = userService.findById(userId);

        var newAccessTokenData = this.createAccessJwt(user.getUsername(),
                user.getRoles().stream()
                .map(Role::getAuthority)
                .collect(Collectors.toSet()));

        var newRefreshTokenData = this.createRefreshJwt(userId);

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(
                    (newRefreshTokenData.refreshJwt() + this.refreshTokenStoragePepper)
                            .getBytes(StandardCharsets.UTF_8)
            );

            var hashedRefreshToken = Base64.getEncoder().encodeToString(hashBytes);

            RefreshTokenEntry newRefreshEntry = RefreshTokenEntry.builder()
                    .jti(newRefreshTokenData.jti())
                    .hashedToken(hashedRefreshToken)
                    .expiry(newRefreshTokenData.expiration())
                    .build();

            // Delete the old refresh token, then save new one.
            refreshTokenRepo.delete(originalRefreshEntry);
            refreshTokenRepo.save(newRefreshEntry);

            return new  TokenPairResponse(newAccessTokenData, newRefreshTokenData);


        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hash type for refresh token storage is wrong!");
        }

    }

    @Override
    public void persistNewRefreshToken(RefreshJwtData refreshJwtData) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(
                    (refreshJwtData.refreshJwt() + this.refreshTokenStoragePepper)
                            .getBytes(StandardCharsets.UTF_8)
            );

            var hashedRefreshToken = Base64.getEncoder().encodeToString(hashBytes);

            RefreshTokenEntry newRefreshEntry = RefreshTokenEntry.builder()
                    .jti(refreshJwtData.jti())
                    .hashedToken(hashedRefreshToken)
                    .expiry(refreshJwtData.expiration())
                    .build();

            refreshTokenRepo.save(newRefreshEntry);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hash type for refresh token storage is wrong!");
        }

    }

}
