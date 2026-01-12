package com.cdcrane.ekkochatsrv.auth.internal;

import com.cdcrane.ekkochatsrv.auth.dto.TokenPairResponse;
import com.cdcrane.ekkochatsrv.users.dto.UserDTO;
import com.cdcrane.ekkochatsrv.users.internal.ApplicationUser;
import com.cdcrane.ekkochatsrv.users.api.UserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtUseCase jwtUseCase;
    private final UserUseCase userUseCase;

    public TokenPairResponse login(String usernameOrEmail, String password) {

        Authentication auth = new UsernamePasswordAuthenticationToken(usernameOrEmail, password);

        try {

            Authentication authentication = authManager.authenticate(auth);

            UserDTO user = userUseCase.findByUsernameOrEmail(usernameOrEmail);

            var accessTokenData = jwtUseCase.createAccessJwt(authentication);
            var refreshTokenData = jwtUseCase.createRefreshJwt(user.userId());

            jwtUseCase.persistNewRefreshToken(refreshTokenData);

            return new  TokenPairResponse(accessTokenData, refreshTokenData);

        } catch (AuthenticationException e) {

            log.warn("Authentication failed for user {}", usernameOrEmail);

            // TODO: Change for a custom exception.
            throw new RuntimeException("Invalid username or password provided.");

        }

    }
}
