package com.cdcrane.ekkochatsrv.auth;

import com.cdcrane.ekkochatsrv.auth.dto.JwtData;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;

public interface JwtUseCase {

    JwtData createJwt(Authentication auth);

    Claims verifyJwt(String jwt);
}
