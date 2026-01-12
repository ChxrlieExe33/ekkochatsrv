package com.cdcrane.ekkochatsrv.users.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<ApplicationUser, UUID> {

    @Query("SELECT u FROM ApplicationUser u JOIN FETCH u.roles WHERE u.userId = ?1")
    Optional<ApplicationUser> findByUserId(UUID userId);

    @Query("SELECT u FROM ApplicationUser u JOIN FETCH u.roles WHERE u.username = ?1 OR u.email = ?1")
    Optional<ApplicationUser> findByEmailOrUsernameWithRoles(String username);
}
