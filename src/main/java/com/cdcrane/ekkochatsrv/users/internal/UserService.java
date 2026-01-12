package com.cdcrane.ekkochatsrv.users.internal;

import com.cdcrane.ekkochatsrv.users.api.UserUseCase;
import com.cdcrane.ekkochatsrv.users.dto.RegisterAccountRequest;
import com.cdcrane.ekkochatsrv.users.dto.UserDTO;
import com.cdcrane.ekkochatsrv.users.exceptions.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class UserService implements UserUseCase {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepo;
    private final Integer minutes10ms = 600000;

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

    @Override
    @Transactional
    public void registerUser(RegisterAccountRequest request) {

        // TODO: Add logic to check if username / email is already taken. Also add logic to check for XSS with JSoup.

        var hash = passwordEncoder.encode(request.password());

        Role userRole = roleRepo.getUserRole();

        var account = ApplicationUser.builder()
                .username(request.username())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .password(hash)
                .email(request.email())
                .enabled(false) // Disabled until email verification is complete.
                .verificationCode(this.generateVerificationCode())
                .verificationCodeExpiration(new Date(System.currentTimeMillis() + minutes10ms))
                .roles(Set.of(userRole))
                .build();

        userRepo.save(account);

    }

    private Integer generateVerificationCode() {

        SecureRandom random = new SecureRandom();
        return random.nextInt(900000) + 100000;

    }
}
