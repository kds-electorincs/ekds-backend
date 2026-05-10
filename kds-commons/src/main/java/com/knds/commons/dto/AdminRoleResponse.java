package com.knds.commons.dto;

import com.knds.commons.security.AdminPage;

import java.time.OffsetDateTime;
import java.util.Set;

public record AdminRoleResponse(
        Long id,
        String name,
        String description,
        Set<AdminPage> pages,
        Long createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) { }
