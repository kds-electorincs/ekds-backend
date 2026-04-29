package com.knds.service;


import com.knds.entities.security.RefreshToken;
import com.knds.entities.security.User;

public interface RefreshTokenService {

    /** Mints a fresh refresh token, persists hash, returns the raw value to send to the client. */
    IssuedRefreshToken issue(User user, String userAgent, String ipAddress);

    /** Validates + rotates. Returns the new pair. Throws on reuse / invalid / expired. */
    IssuedRefreshToken rotate(String rawToken, String userAgent, String ipAddress);

    /** Logout — revoke the supplied token (no error if already revoked). */
    void revoke(String rawToken);

    /** Pair returned to the caller: raw token to send to client, entity persisted. */
    record IssuedRefreshToken(String rawToken, RefreshToken entity) { }
}