package com.cdcrane.ekkochatsrv.auth.filter;

import com.cdcrane.ekkochatsrv.auth.internal.JwtUseCase;
import com.cdcrane.ekkochatsrv.auth.internal.SecurityConfig;
import com.cdcrane.ekkochatsrv.auth.enums.JwtTypes;
import com.cdcrane.ekkochatsrv.auth.enums.NamedJwtClaims;
import com.cdcrane.ekkochatsrv.auth.exceptions.BadJwtException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AccessTokenValidatorFilter extends OncePerRequestFilter {

    private final JwtUseCase jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String jwt = request.getHeader("Authorization");

        if (jwt != null && jwt.startsWith("Bearer ")) {

            String token = jwt.substring(7);

            try {

                Claims claims = jwtService.verifyAccessJwt(token);

                String username = claims.get(NamedJwtClaims.USERNAME.name(), String.class);
                String authorities = claims.get(NamedJwtClaims.AUTHORITIES.name(), String.class);

                String tokenType = claims.get(NamedJwtClaims.TYPE.name(), String.class);

                if (!tokenType.equals(JwtTypes.ACCESS.name())) {

                    throw new BadCredentialsException("You cannot use a refresh token for accessing secured endpoints.");
                }

                // Setting credentials to null means the user is already authenticated.
                Authentication auth = new UsernamePasswordAuthenticationToken(username, null,
                        AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (BadJwtException ex) { // Translate to BadCredentials since that can be handled in the security layer.
                throw new BadCredentialsException(ex.getMessage());
            }


        } else {

            throw new BadCredentialsException("Invalid JWT Token, must follow the 'Bearer <token>' format");
        }

        filterChain.doFilter(request, response);

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {


        for (String uri : SecurityConfig.PUBLIC_URIS){
            if (request.getRequestURI().equals(uri)){
                return true;
            }
        }

        return false;
    }
}
