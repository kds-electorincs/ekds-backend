package com.knds.commons.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record AdminUserResponse(
        Long id,
        String email,
        String fullName,
        boolean enabled,
        boolean superAdmin,
        List<AssignedRole> assignedRoles,
        OffsetDateTime createdAt
) {
    public record AssignedRole(
            Long id,
            String name,
            OffsetDateTime grantedAt,
            Long grantedById
    ) { }
}