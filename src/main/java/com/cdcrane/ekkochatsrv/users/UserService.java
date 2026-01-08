package com.cdcrane.ekkochatsrv.users;

import com.cdcrane.ekkochatsrv.users.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

}
