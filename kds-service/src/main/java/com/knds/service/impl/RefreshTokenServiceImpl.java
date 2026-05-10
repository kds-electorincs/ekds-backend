package com.knds.service.impl;

import com.knds.commons.exceptions.InvalidRefreshTokenException;
import com.knds.entities.security.RefreshToken;
import com.knds.entities.security.User;
import com.knds.repository.security.RefreshTokenRepository;
import com.knds.service.security.JwtProperties;
import com.knds.service.RefreshTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final SecureRandom RNG = new SecureRandom();
    private static final int RAW_TOKEN_BYTES = 32;   // 256 bits — collision-resistant

    private final RefreshTokenRepository repo;
    private final JwtProperties jwtProps;

    public RefreshTokenServiceImpl(RefreshTokenRepository repo, JwtProperties jwtProps) {
        this.repo = repo;
        this.jwtProps = jwtProps;
    }

    @Override
    @Transactional
    public IssuedRefreshToken issue(User user, String userAgent, String ipAddress) {
        return mint(user, userAgent, ipAddress);
    }

    @Override
    @Transactional
    public IssuedRefreshToken rotate(String rawToken, String userAgent, String ipAddress) {
        String hash = sha256(rawToken);

        RefreshToken stored = repo.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Unknown refresh token"));

        // ── REUSE DETECTION ─────────────────────────────────────────
        // Token was already revoked. Either the user logged out and is now retrying,
        // or someone stole this token and is trying to use it after it was rotated.
        // Either way: nuke ALL active tokens for this user. Force a fresh login.
        if (stored.getRevokedAt() != null) {
            repo.revokeAllActiveForUser(stored.getUser().getId(), OffsetDateTime.now());
            throw new InvalidRefreshTokenException("Refresh token reuse detected — all sessions terminated");
        }

        if (stored.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        // Mint the replacement BEFORE marking the old one revoked,
        // so we can record its hash in `replaced_by`.
            IssuedRefreshToken next = mint(stored.getUser(), userAgent, ipAddress);
        stored.revoke(next.entity().getTokenHash());

        return next;
    }

    @Override
    @Transactional
    public void revoke(String rawToken) {
        String hash = sha256(rawToken);
        repo.findByTokenHash(hash).ifPresent(rt -> {
            if (rt.getRevokedAt() == null) rt.revoke(null);
        });
    }

    // ── helpers ────────────────────────────────────────────────────

    private IssuedRefreshToken mint(User user, String userAgent, String ipAddress) {
        byte[] raw = new byte[RAW_TOKEN_BYTES];
        RNG.nextBytes(raw);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        OffsetDateTime expiresAt = OffsetDateTime.now().plus(jwtProps.refreshTokenTtl());
        RefreshToken entity = new RefreshToken(user, sha256(rawToken), expiresAt);
        entity.setUserAgent(truncate(userAgent, 255));
        entity.setIpAddress(ipAddress);

        repo.save(entity);
        return new IssuedRefreshToken(rawToken, entity);
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes());
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}