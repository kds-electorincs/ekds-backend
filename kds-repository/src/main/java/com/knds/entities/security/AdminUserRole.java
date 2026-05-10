package com.knds.entities.security;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "admin_user_roles")
public class AdminUserRole {

    @EmbeddedId
    private Id id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id")
    private AdminRole role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "granted_by", nullable = false, updatable = false)
    private User grantedBy;

    @Column(name = "granted_at", nullable = false, updatable = false)
    private OffsetDateTime grantedAt;

    protected AdminUserRole() {}

    public AdminUserRole(User user, AdminRole role, User grantedBy) {
        this.id = new Id(user.getId(), role.getId());
        this.user = user;
        this.role = role;
        this.grantedBy = grantedBy;
    }

    @PrePersist
    void onCreate() {
        this.grantedAt = OffsetDateTime.now();
    }

    public Id getId() { return id; }
    public User getUser() { return user; }
    public AdminRole getRole() { return role; }
    public User getGrantedBy() { return grantedBy; }
    public OffsetDateTime getGrantedAt() { return grantedAt; }

    /** Composite PK for admin_user_roles. */
    @Embeddable
    public static class Id implements Serializable {

        @Column(name = "user_id")
        private Long userId;

        @Column(name = "role_id")
        private Long roleId;

        protected Id() {}

        public Id(Long userId, Long roleId) {
            this.userId = userId;
            this.roleId = roleId;
        }

        public Long getUserId() { return userId; }
        public Long getRoleId() { return roleId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id other)) return false;
            return Objects.equals(userId, other.userId) && Objects.equals(roleId, other.roleId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, roleId);
        }
    }
}