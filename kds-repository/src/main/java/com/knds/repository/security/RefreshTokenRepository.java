package com.knds.repository.security;

import com.knds.entities.security.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
        UPDATE RefreshToken r
        SET r.revokedAt = :now
        WHERE r.user.id = :userId AND r.revokedAt IS NULL
    """)
    int revokeAllActiveForUser(@Param("userId") Long userId, @Param("now") OffsetDateTime now);
}