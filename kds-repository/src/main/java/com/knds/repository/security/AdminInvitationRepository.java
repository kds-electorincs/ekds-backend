package com.knds.repository.security;

import com.knds.entities.security.AdminInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface AdminInvitationRepository extends JpaRepository<AdminInvitation, Long> {

    Optional<AdminInvitation> findByTokenHash(String tokenHash);

    /** Used to block creating a second invitation for an email that has one outstanding. */
    @Query("""
        SELECT i FROM AdminInvitation i
        WHERE LOWER(i.email) = LOWER(:email) AND i.status = com.knds.entities.security.AdminInvitation.Status.PENDING
    """)
    Optional<AdminInvitation> findPendingByEmailIgnoreCase(@Param("email") String email);

    List<AdminInvitation> findAllByOrderByCreatedAtDesc();

    /** Bulk-mark expired pending invitations. Used by the scheduled cleanup. */
    @Modifying
    @Query("""
        UPDATE AdminInvitation i
        SET i.status = com.knds.entities.security.AdminInvitation.Status.EXPIRED
        WHERE i.status = com.knds.entities.security.AdminInvitation.Status.PENDING
          AND i.expiresAt < :now
    """)
    int markExpiredBefore(@Param("now") OffsetDateTime now);
}