package com.knds.commons.dto;

import java.time.OffsetDateTime;

public record AuthTokens(
        String accessToken,
        String refreshToken,
        OffsetDateTime accessTokenExpiresAt,
        OffsetDateTime refreshTokenExpiresAt
) { }