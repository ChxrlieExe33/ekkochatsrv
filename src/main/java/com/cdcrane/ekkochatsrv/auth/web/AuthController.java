package com.cdcrane.ekkochatsrv.auth.web;

import com.cdcrane.ekkochatsrv.auth.dto.LoginRequest;
import com.cdcrane.ekkochatsrv.auth.dto.TokenPairResponse;
import com.cdcrane.ekkochatsrv.auth.exceptions.BadJwtException;
import com.cdcrane.ekkochatsrv.auth.internal.AuthService;
import com.cdcrane.ekkochatsrv.auth.internal.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final String BEARER = "Bearer ";
    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> login(@RequestBody @Valid LoginRequest loginRequest) {

        var tokens = authService.login(loginRequest.usernameOrEmail(), loginRequest.password());

        return ResponseEntity.ok(tokens);

    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@RequestHeader(name="Authorization") String refreshToken) {

        if (!refreshToken.startsWith(BEARER)) {
            throw new BadJwtException("Please follow the Bearer prefix format for tokens.");
        }

        var res = jwtService.refreshBothTokens(refreshToken.substring(BEARER.length()));

        return ResponseEntity.ok(res);

    }

    @GetMapping
    public String testProtected() {
        return "You are allowed";
    }

}
