package com.knds.repository.security;

import com.knds.entities.security.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRoleRepository extends JpaRepository<AdminRole, Long> {

    Optional<AdminRole> findByName(String name);

    boolean existsByName(String name);
}