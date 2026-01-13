package com.cdcrane.ekkochatsrv.users.internal;

import com.cdcrane.ekkochatsrv.users.api.UserUseCase;
import com.cdcrane.ekkochatsrv.users.dto.RegisterAccountRequest;
import com.cdcrane.ekkochatsrv.users.dto.UserDTO;
import com.cdcrane.ekkochatsrv.users.dto.VerifyEmailRequest;
import com.cdcrane.ekkochatsrv.users.events.AccountRegisteredEvent;
import com.cdcrane.ekkochatsrv.users.events.EmailVerificationFailEvent;
import com.cdcrane.ekkochatsrv.users.exceptions.IdentityTakenException;
import com.cdcrane.ekkochatsrv.users.exceptions.InvalidVerificationException;
import com.cdcrane.ekkochatsrv.users.exceptions.UserAlreadyVerifiedException;
import com.cdcrane.ekkochatsrv.users.exceptions.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher publisher;
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

        if (userRepo.existsByEmail(request.email())) {
            throw new IdentityTakenException("User with email " + request.email() + " already exists!");
        }

        if (userRepo.existsByUsername(request.username())) {
            throw new IdentityTakenException("User with username " + request.username() + " already exists!");
        }

        var hash = passwordEncoder.encode(request.password());

        Role userRole = roleRepo.getUserRole();

        var account = ApplicationUser.builder()
                .username(removeHtml(request.username()))
                .firstName(removeHtml(request.firstName()))
                .lastName(removeHtml(request.lastName()))
                .password(hash)
                .email(request.email())
                .enabled(false) // Disabled until email verification is complete.
                .verificationCode(this.generateVerificationCode())
                .verificationCodeExpiration(new Date(System.currentTimeMillis() + minutes10ms))
                .roles(Set.of(userRole))
                .build();

        var saved = userRepo.save(account);

        publisher.publishEvent(new AccountRegisteredEvent(saved.getUserId(), saved.getUsername(), saved.getEmail(), saved.getVerificationCode()));

    }

    @Override
    @Transactional(dontRollbackOn = InvalidVerificationException.class) // Can't roll back on that exception, or code change won't persist.
    public void handleEmailVerification(VerifyEmailRequest request) {

        var user = userRepo.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + request.email()));

        if (user.getEnabled()) throw new UserAlreadyVerifiedException("User is already verified!");

        var now = new Date();

        if (now.after(user.getVerificationCodeExpiration())) {

            // Generate new code, persist, and send out event.
            user.setVerificationCode(this.generateVerificationCode());
            user.setVerificationCodeExpiration(new Date(System.currentTimeMillis() + minutes10ms));
            userRepo.save(user);

            publisher.publishEvent(new EmailVerificationFailEvent(user.getUsername(), user.getEmail(), user.getVerificationCode()));

            throw new InvalidVerificationException("Verification code expired, a new one has been generated.");

        }

        if (!user.getVerificationCode().equals(request.code())) {

            // Generate new code, persist, and send out event.
            user.setVerificationCode(this.generateVerificationCode());
            user.setVerificationCodeExpiration(new Date(System.currentTimeMillis() + minutes10ms));
            userRepo.save(user);

            publisher.publishEvent(new EmailVerificationFailEvent(user.getEmail(), user.getUsername(), user.getVerificationCode()));

            throw new InvalidVerificationException("Verification code is incorrect, a new one has been generated.");

        } else {

            user.setVerificationCode(null);
            user.setVerificationCodeExpiration(null);
            user.setEnabled(true);

            userRepo.save(user);

        }

    }


    private Integer generateVerificationCode() {

        SecureRandom random = new SecureRandom();
        return random.nextInt(900000) + 100000;

    }

    private String removeHtml(String input) {

        return Jsoup.clean(input, Safelist.none());
    }
}
