package com.knds.commons.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record UserProfileResponse(
        Long id,
        String email,
        String fullName,
        String phone,
        boolean emailVerified,
        List<String> roles,
        OffsetDateTime createdAt
) { }