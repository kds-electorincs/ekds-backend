package com.knds.service.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Generates opaque random tokens for refresh tokens, invitations, etc.
 *
 * NOT a Spring bean — pure static utility, no state, no DI needed.
 * Keeps RefreshTokenServiceImpl and AdminInvitationServiceImpl from
 * duplicating the same SecureRandom + Base64 + SHA-256 ceremony.
 */
public final class TokenGenerator {

    private static final SecureRandom RNG = new SecureRandom();
    private static final int DEFAULT_BYTES = 32;   // 256 bits

    private TokenGenerator() {}

    /** Generate a URL-safe Base64-encoded token of the default length. */
    public static String generateRawToken() {
        return generateRawToken(DEFAULT_BYTES);
    }

    /** Generate a URL-safe Base64-encoded token with the given entropy in bytes. */
    public static String generateRawToken(int bytes) {
        byte[] buf = new byte[bytes];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    /** SHA-256 hex hash of the raw token, for storage. */
    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}