package com.cdcrane.ekkochatsrv.auth;

import com.cdcrane.ekkochatsrv.auth.dto.AccessJwtData;
import com.cdcrane.ekkochatsrv.auth.dto.RefreshJwtData;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface JwtUseCase {

    AccessJwtData createAccessJwt(Authentication auth);

    RefreshJwtData createRefreshJwt(UUID userId);

    Claims verifyAccessJwt(String jwt);

    Claims verifyRefreshJwt(String jwt);
}
