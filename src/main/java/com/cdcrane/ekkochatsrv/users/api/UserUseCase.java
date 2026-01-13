package com.cdcrane.ekkochatsrv.users.api;

import com.cdcrane.ekkochatsrv.users.dto.RegisterAccountRequest;
import com.cdcrane.ekkochatsrv.users.dto.UserDTO;
import com.cdcrane.ekkochatsrv.users.dto.VerifyEmailRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

public interface UserUseCase extends UserDetailsService {

    UserDTO findById(UUID id);

    UserDTO findByUsernameOrEmail(String usernameOrEmail);

    void registerUser(RegisterAccountRequest request);

    void handleEmailVerification(VerifyEmailRequest request);
}
