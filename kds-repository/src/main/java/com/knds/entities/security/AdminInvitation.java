package com.knds.entities.security;

import com.knds.entities.security.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "admin_invitations")
public class AdminInvitation {

    public enum Status { PENDING, ACCEPTED, EXPIRED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.PENDING;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_user_id")
    private User acceptedUser;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by")
    private User cancelledBy;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "admin_invitation_roles",
            joinColumns        = @JoinColumn(name = "invitation_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<AdminRole> roles = new HashSet<>();

    protected AdminInvitation() {}

    public AdminInvitation(String email, String tokenHash, OffsetDateTime expiresAt, User createdBy) {
        this.email      = email;
        this.tokenHash  = tokenHash;
        this.expiresAt  = expiresAt;
        this.createdBy  = createdBy;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public boolean isPending() {
        return status == Status.PENDING && expiresAt.isAfter(OffsetDateTime.now());
    }

    public void markAccepted(User createdUser) {
        this.status        = Status.ACCEPTED;
        this.acceptedAt    = OffsetDateTime.now();
        this.acceptedUser  = createdUser;
    }

    public void markCancelled(User by) {
        this.status        = Status.CANCELLED;
        this.cancelledAt   = OffsetDateTime.now();
        this.cancelledBy   = by;
    }

    public void markExpired() {
        this.status = Status.EXPIRED;
    }

    public void replaceToken(String newTokenHash, OffsetDateTime newExpiresAt) {
        this.tokenHash = newTokenHash;
        this.expiresAt = newExpiresAt;
    }

    // ── getters ────────────────────────────────────────────────────
    public Long getId()                       { return id; }
    public String getEmail()                  { return email; }
    public String getTokenHash()              { return tokenHash; }
    public Status getStatus()                 { return status; }
    public User getCreatedBy()                { return createdBy; }
    public OffsetDateTime getCreatedAt()      { return createdAt; }
    public OffsetDateTime getExpiresAt()      { return expiresAt; }
    public OffsetDateTime getAcceptedAt()     { return acceptedAt; }
    public User getAcceptedUser()             { return acceptedUser; }
    public OffsetDateTime getCancelledAt()    { return cancelledAt; }
    public User getCancelledBy()              { return cancelledBy; }
    public Set<AdminRole> getRoles()          { return roles; }
    public void setRoles(Set<AdminRole> roles){ this.roles = roles; }
}