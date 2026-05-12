package com.knds.service.jobs;

import com.knds.repository.security.AdminInvitationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Periodically marks expired pending invitations.
 *
 * The lazy expiry check in AdminInvitationServiceImpl.lookupActivePending also flips
 * status when an invitee clicks an expired link — this job handles the case where
 * nobody ever clicks. Keeps `status` accurate for the list endpoint.
 */
@Component
public class InvitationCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(InvitationCleanupJob.class);

    private final AdminInvitationRepository invitationRepo;

    public InvitationCleanupJob(AdminInvitationRepository invitationRepo) {
        this.invitationRepo = invitationRepo;
    }

    /**
     * Runs once an hour. Hourly resolution is fine — an invitation expiring
     * a few minutes "early" or "late" has no security implications since the
     * lazy check in the accept flow always validates `expires_at` directly.
     */
    @Scheduled(cron = "${kds.invitation.cleanup-cron}")
    @Transactional
    public void markExpiredInvitations() {
        int updated = invitationRepo.markExpiredBefore(OffsetDateTime.now());
        if (updated > 0) {
            log.info("Marked {} invitation(s) as EXPIRED", updated);
        }
    }
}