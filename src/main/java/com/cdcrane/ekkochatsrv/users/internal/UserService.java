package com.cdcrane.ekkochatsrv.users.internal;

import com.cdcrane.ekkochatsrv.users.api.UserUseCase;
import com.cdcrane.ekkochatsrv.users.dto.UserDTO;
import com.cdcrane.ekkochatsrv.users.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class UserService implements UserUseCase {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        var user = userRepo.findByEmailOrUsernameWithRoles(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        return new User(user.getUsername(), user.getPassword(), user.getEnabled(), true, true, true,
                user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getAuthority()))
                        .toList()
        );

    }

    @Override
    public UserDTO findById(UUID id) {

        var u = userRepo.findByUserId(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + id));

        return new UserDTO(u.getUserId(), u.getUsername(),
                u.getFirstName(), u.getLastName(), u.getEmail(),
                u.getRoles().stream()
                        .map(Role::getAuthority)
                        .collect(Collectors.toSet()));
    }

    @Override
    public UserDTO findByUsernameOrEmail(String usernameOrEmail) {

        var u = userRepo.findByEmailOrUsernameWithRoles(usernameOrEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + usernameOrEmail));

        return new UserDTO(u.getUserId(), u.getUsername(),
                u.getFirstName(), u.getLastName(), u.getEmail(),
                u.getRoles().stream()
                        .map(Role::getAuthority)
                        .collect(Collectors.toSet()));
    }
}
