package com.cdcrane.ekkochatsrv.users.api;

import com.cdcrane.ekkochatsrv.users.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

public interface UserUseCase extends UserDetailsService {

    UserDTO findById(UUID id);

    UserDTO findByUsernameOrEmail(String usernameOrEmail);
}
