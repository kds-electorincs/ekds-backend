package com.knds.service.impl;

import com.knds.commons.dto.AdminInvitationResponse;
import com.knds.commons.dto.AdminInvitationResponse.AssignedRoleSummary;
import com.knds.commons.dto.CreateInvitationRequest;
import com.knds.commons.exceptions.AdminRoleNotFoundException;
import com.knds.commons.exceptions.EmailAlreadyRegisteredException;
import com.knds.commons.exceptions.InvitationNotFoundException;
import com.knds.commons.exceptions.InvitationNotPendingException;
import com.knds.commons.exceptions.PendingInvitationExistsException;
import com.knds.commons.exceptions.UserNotFoundException;
import com.knds.entities.security.AdminInvitation;
import com.knds.entities.security.AdminRole;
import com.knds.entities.security.User;
import com.knds.repository.security.AdminInvitationRepository;
import com.knds.repository.security.AdminRoleRepository;
import com.knds.repository.security.UserRepository;
import com.knds.service.security.InvitationProperties;
import com.knds.service.security.TokenGenerator;
import com.knds.service.AdminInvitationService;
import com.knds.service.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminInvitationServiceImpl implements AdminInvitationService {

    private static final Duration INVITATION_TTL = Duration.ofDays(7);

    private final AdminInvitationRepository invitationRepo;
    private final AdminRoleRepository roleRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;
    private final InvitationProperties invitationProps;

    public AdminInvitationServiceImpl(AdminInvitationRepository invitationRepo,
                                      AdminRoleRepository roleRepo,
                                      UserRepository userRepo,
                                      EmailService emailService,
                                      InvitationProperties invitationProps) {
        this.invitationRepo  = invitationRepo;
        this.roleRepo        = roleRepo;
        this.userRepo        = userRepo;
        this.emailService    = emailService;
        this.invitationProps = invitationProps;
    }

    @Override
    @Transactional
    public AdminInvitationResponse create(CreateInvitationRequest req, Long createdByUserId) {
        String email = req.email().toLowerCase();

        // Block 1: email already belongs to an existing user
        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }

        // Block 2: email already has a pending invitation
        invitationRepo.findPendingByEmailIgnoreCase(email).ifPresent(existing -> {
            throw new PendingInvitationExistsException(email);
        });

        // Resolve all role IDs to real entities, fail fast if any missing
        Set<AdminRole> roles = resolveRoles(req.roleIds());

        User createdBy = userRepo.findById(createdByUserId)
                .orElseThrow(() -> new UserNotFoundException(createdByUserId));

        // Mint token + persist
        String rawToken = TokenGenerator.generateRawToken();
        String tokenHash = TokenGenerator.sha256Hex(rawToken);
        OffsetDateTime expiresAt = OffsetDateTime.now().plus(INVITATION_TTL);

        AdminInvitation invitation = new AdminInvitation(email, tokenHash, expiresAt, createdBy);
        invitation.setRoles(roles);
        invitationRepo.save(invitation);

        // Send (stub logs to console)
        sendInvitationEmail(invitation, rawToken, createdBy);

        return toResponse(invitation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminInvitationResponse> listAll() {
        return invitationRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AdminInvitationResponse resend(Long invitationId, Long requestedByUserId) {
        AdminInvitation invitation = getInvitationOrThrow(invitationId);

        // Resend only meaningful for PENDING (or recently expired). For simplicity:
        // we allow resending PENDING invitations only — others require creating a new one.
        if (invitation.getStatus() != AdminInvitation.Status.PENDING) {
            throw new InvitationNotPendingException(invitationId, invitation.getStatus().name());
        }

        // Re-check user hasn't been registered in the meantime
        if (userRepo.existsByEmailIgnoreCase(invitation.getEmail())) {
            throw new EmailAlreadyRegisteredException(invitation.getEmail());
        }

        // Generate a fresh token, replace the old one, reset expiry from now
        String newRaw   = TokenGenerator.generateRawToken();
        String newHash  = TokenGenerator.sha256Hex(newRaw);
        OffsetDateTime newExpiresAt = OffsetDateTime.now().plus(INVITATION_TTL);
        invitation.replaceToken(newHash, newExpiresAt);

        User resendingUser = userRepo.findById(requestedByUserId)
                .orElseThrow(() -> new UserNotFoundException(requestedByUserId));

        sendInvitationEmail(invitation, newRaw, resendingUser);

        return toResponse(invitation);
    }

    @Override
    @Transactional
    public void cancel(Long invitationId, Long cancelledByUserId) {
        AdminInvitation invitation = getInvitationOrThrow(invitationId);

        if (invitation.getStatus() != AdminInvitation.Status.PENDING) {
            throw new InvitationNotPendingException(invitationId, invitation.getStatus().name());
        }

        User cancelledBy = userRepo.findById(cancelledByUserId)
                .orElseThrow(() -> new UserNotFoundException(cancelledByUserId));

        invitation.markCancelled(cancelledBy);
    }

    // ── helpers ────────────────────────────────────────────────────

    private Set<AdminRole> resolveRoles(Set<Long> roleIds) {
        Set<AdminRole> roles = new HashSet<>();
        for (Long id : roleIds) {
            roles.add(roleRepo.findById(id)
                    .orElseThrow(() -> new AdminRoleNotFoundException(id)));
        }
        return roles;
    }

    private AdminInvitation getInvitationOrThrow(Long id) {
        return invitationRepo.findById(id)
                .orElseThrow(() -> new InvitationNotFoundException(id));
    }

    private void sendInvitationEmail(AdminInvitation invitation, String rawToken, User invitedBy) {
        String acceptUrl = invitationProps.buildAcceptUrl(rawToken);
        emailService.sendAdminInvitation(invitation.getEmail(), acceptUrl, invitedBy.getFullName());
    }

    private AdminInvitationResponse toResponse(AdminInvitation inv) {
        List<AssignedRoleSummary> roles = inv.getRoles().stream()
                .map(r -> new AssignedRoleSummary(r.getId(), r.getName()))
                .toList();

        return new AdminInvitationResponse(
                inv.getId(),
                inv.getEmail(),
                inv.getStatus().name(),
                roles,
                inv.getCreatedBy().getId(),
                inv.getCreatedAt(),
                inv.getExpiresAt(),
                inv.getAcceptedAt(),
                inv.getAcceptedUser() != null ? inv.getAcceptedUser().getId() : null,
                inv.getCancelledAt(),
                inv.getCancelledBy() != null ? inv.getCancelledBy().getId() : null
        );
    }
}