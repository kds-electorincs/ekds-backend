package com.knds.service.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kds.frontend")
public record InvitationProperties(
        String baseUrl,
        String acceptInvitePath
) {
    public String buildAcceptUrl(String rawToken) {
        // Trim trailing slash on baseUrl, leading slash on path — defensive.
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = acceptInvitePath.startsWith("/") ? acceptInvitePath : "/" + acceptInvitePath;
        return base + path + "?token=" + rawToken;
    }
}