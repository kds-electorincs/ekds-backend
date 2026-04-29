package com.knds.entities.security;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, length = 32)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Role() {}

    public Short getId() { return id; }
    public String getName() { return name; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
