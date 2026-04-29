package com.knds.entities.security;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private OffsetDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "replaced_by", length = 64)
    private String replacedBy;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "ip_address", columnDefinition = "inet")
    private String ipAddress;

    protected RefreshToken() {}

    public RefreshToken(User user, String tokenHash, OffsetDateTime expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void onCreate() {
        this.issuedAt = OffsetDateTime.now();
    }

    public boolean isActive() {
        return revokedAt == null && expiresAt.isAfter(OffsetDateTime.now());
    }

    public void revoke(String replacedByHash) {
        this.revokedAt = OffsetDateTime.now();
        this.replacedBy = replacedByHash;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getTokenHash() { return tokenHash; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public OffsetDateTime getRevokedAt() { return revokedAt; }
    public String getReplacedBy() { return replacedBy; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}