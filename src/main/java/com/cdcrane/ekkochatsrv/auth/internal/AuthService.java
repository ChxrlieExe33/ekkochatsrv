package com.cdcrane.ekkochatsrv.auth.internal;

import com.cdcrane.ekkochatsrv.auth.dto.TokenPairResponse;
import com.cdcrane.ekkochatsrv.auth.exceptions.BadAuthenticationException;
import com.cdcrane.ekkochatsrv.users.dto.UserDTO;
import com.cdcrane.ekkochatsrv.users.api.UserUseCase;
import com.cdcrane.ekkochatsrv.users.principal.EkkoUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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

            EkkoUserPrincipal principal = (EkkoUserPrincipal) authentication.getPrincipal();

            if (principal == null || principal.getUserId() == null) {
                throw new BadAuthenticationException("Authentication principal configured incorrectly.");
            }

            var accessTokenData = jwtUseCase.createAccessJwt(authentication, principal.getUserId());
            var refreshTokenData = jwtUseCase.createRefreshJwt(principal.getUserId());

            jwtUseCase.persistNewRefreshToken(refreshTokenData);

            return new  TokenPairResponse(accessTokenData, refreshTokenData);

        } catch (DisabledException e) {

            log.info("User {} tried to login when their account is not verified yet.", usernameOrEmail);
            throw new BadAuthenticationException("Account has not been verified and enabled yet");

        } catch (BadCredentialsException e) {

            log.info("User {} tried to log in with invalid credentials", usernameOrEmail);
            throw new BadAuthenticationException("Invalid credentials.");

        } catch (AuthenticationException e) {

            log.warn("Authentication failed for user {}", usernameOrEmail);

            throw new BadAuthenticationException("Something went wrong with your authentication.");

        }

    }
}
