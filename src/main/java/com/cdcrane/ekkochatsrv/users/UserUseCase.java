package com.cdcrane.ekkochatsrv.users;

import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

public interface UserUseCase extends UserDetailsService {

    ApplicationUser findById(UUID id);

    ApplicationUser findByUsernameOrEmail(String usernameOrEmail);
}
