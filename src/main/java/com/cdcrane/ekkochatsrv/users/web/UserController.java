package com.cdcrane.ekkochatsrv.users.web;

import com.cdcrane.ekkochatsrv.users.api.UserUseCase;
import com.cdcrane.ekkochatsrv.users.dto.RegisterAccountRequest;
import com.cdcrane.ekkochatsrv.users.dto.VerifyEmailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;

    @PostMapping("/register")
    public ResponseEntity<Void> handleRegister(@RequestBody RegisterAccountRequest req) {

        userUseCase.registerUser(req);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> handleVerification(@RequestBody VerifyEmailRequest req) {

        userUseCase.handleEmailVerification(req);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

}
