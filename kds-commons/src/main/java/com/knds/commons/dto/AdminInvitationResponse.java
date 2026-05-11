package com.knds.commons.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record AdminInvitationResponse(
        Long id,
        String email,
        String status,
        List<AssignedRoleSummary> roles,
        Long createdById,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt,
        OffsetDateTime acceptedAt,
        Long acceptedUserId,
        OffsetDateTime cancelledAt,
        Long cancelledById
) {
    public record AssignedRoleSummary(Long id, String name) { }
}