package com.cdcrane.ekkochatsrv.auth.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntry, UUID> {

    Optional<RefreshTokenEntry> findByJti(UUID jti);
}
