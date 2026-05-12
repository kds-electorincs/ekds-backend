package com.knds.service.impl;

import com.knds.commons.dto.AdminUserResponse;
import com.knds.commons.dto.AdminUserResponse.AssignedRole;
import com.knds.commons.exceptions.AdminRoleAlreadyAssignedException;
import com.knds.commons.exceptions.AdminRoleNotAssignedException;
import com.knds.commons.exceptions.AdminRoleNotFoundException;
import com.knds.commons.exceptions.SuperAdminProtectedException;
import com.knds.commons.exceptions.UserNotAdminException;
import com.knds.commons.exceptions.UserNotFoundException;
import com.knds.entities.security.*;
import com.knds.repository.security.*;
import com.knds.service.AdminAccessService;
import com.knds.service.AdminUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private static final String ROLE_ADMIN       = "ROLE_ADMIN";
    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final AdminRoleRepository adminRoleRepo;
    private final AdminUserRoleRepository adminUserRoleRepo;
    private final AdminAccessService accessService;

    public AdminUserServiceImpl(UserRepository userRepo,
                                RoleRepository roleRepo,
                                AdminRoleRepository adminRoleRepo,
                                AdminUserRoleRepository adminUserRoleRepo,
                                AdminAccessService accessService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.adminRoleRepo = adminRoleRepo;
        this.adminUserRoleRepo = adminUserRoleRepo;
        this.accessService = accessService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> listAdmins() {
        return userRepo.findAll().stream()
                .filter(this::isAdmin)
                .sorted(Comparator.comparing(User::getCreatedAt))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getAdmin(Long userId) {
        User user = getUserOrThrow(userId);
        if (!isAdmin(user)) {
            throw new UserNotAdminException(userId);
        }
        return toResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse promoteToAdmin(Long userId) {
        User user = getUserOrThrow(userId);

        if (isAdmin(user)) {
            return toResponse(user);   // idempotent — already admin
        }

        Role adminRole = roleRepo.findByName(ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not seeded"));
        user.getRoles().add(adminRole);

        return toResponse(user);
    }

    @Override
    @Transactional
    public void demoteFromAdmin(Long userId) {
        User user = getUserOrThrow(userId);

        if (isSuperAdmin(user)) {
            throw new SuperAdminProtectedException(
                    "Cannot demote super admin (user " + userId + ")");
        }

        if (!isAdmin(user)) {
            return;   // idempotent
        }

        user.getRoles().removeIf(r -> ROLE_ADMIN.equals(r.getName()));
        adminUserRoleRepo.deleteByIdUserId(userId);
        accessService.invalidate(userId);
    }

    @Override
    @Transactional
    public AdminUserResponse assignRole(Long userId, Long roleId, Long grantedByUserId) {
        User user = getUserOrThrow(userId);

        if (isSuperAdmin(user)) {
            throw new SuperAdminProtectedException(
                    "Super admin holds all pages implicitly; explicit role assignment is not allowed");
        }

        if (!isAdmin(user)) {
            throw new UserNotAdminException(userId);
        }

        AdminRole role = adminRoleRepo.findById(roleId)
                .orElseThrow(() -> new AdminRoleNotFoundException(roleId));

        if (adminUserRoleRepo.existsByIdUserIdAndIdRoleId(userId, roleId)) {
            throw new AdminRoleAlreadyAssignedException(userId, roleId);
        }

        User grantedBy = getUserOrThrow(grantedByUserId);
        adminUserRoleRepo.save(new AdminUserRole(user, role, grantedBy));

        accessService.invalidate(userId);
        return toResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse revokeRole(Long userId, Long roleId) {
        User user = getUserOrThrow(userId);

        if (isSuperAdmin(user)) {
            throw new SuperAdminProtectedException(
                    "Super admin has no explicit role assignments to revoke");
        }

        if (!adminUserRoleRepo.existsByIdUserIdAndIdRoleId(userId, roleId)) {
            throw new AdminRoleNotAssignedException(userId, roleId);
        }

        adminUserRoleRepo.deleteByIdUserIdAndIdRoleId(userId, roleId);
        accessService.invalidate(userId);

        return toResponse(user);
    }

    // ── helpers ────────────────────────────────────────────────────

    private User getUserOrThrow(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(r -> ROLE_ADMIN.equals(r.getName()));
    }

    private boolean isSuperAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(r -> ROLE_SUPER_ADMIN.equals(r.getName()));
    }

    private AdminUserResponse toResponse(User user) {
        List<AssignedRole> assigned = adminUserRoleRepo.findByUserId(user.getId()).stream()
                .map(aur -> new AssignedRole(
                        aur.getRole().getId(),
                        aur.getRole().getName(),
                        aur.getGrantedAt(),
                        aur.getGrantedBy().getId()))
                .toList();

        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.isEnabled(),
                isSuperAdmin(user),
                assigned,
                user.getCreatedAt()
        );
    }
}