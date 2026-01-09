package com.cdcrane.ekkochatsrv.auth;

import com.cdcrane.ekkochatsrv.auth.dto.AccessJwtData;
import com.cdcrane.ekkochatsrv.auth.dto.RefreshJwtData;
import com.cdcrane.ekkochatsrv.auth.dto.TokenPairResponse;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface JwtUseCase {

    AccessJwtData createAccessJwt(Authentication auth);

    AccessJwtData createAccessJwt(String username, Set<String> roles);

    RefreshJwtData createRefreshJwt(UUID userId);

    Claims verifyAccessJwt(String jwt);

    Claims verifyRefreshJwt(String jwt);

    TokenPairResponse refreshBothTokens(String refreshToken);
}
