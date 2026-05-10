package com.knds.service.impl;

import com.knds.commons.dto.AdminRoleResponse;
import com.knds.commons.dto.CreateAdminRoleRequest;
import com.knds.commons.dto.UpdateAdminRoleRequest;
import com.knds.commons.exceptions.AdminRoleNameConflictException;
import com.knds.commons.exceptions.AdminRoleNotFoundException;
import com.knds.commons.exceptions.UserNotFoundException;
import com.knds.entities.security.AdminRole;
import com.knds.entities.security.User;
import com.knds.repository.security.AdminRoleRepository;
import com.knds.repository.security.UserRepository;
import com.knds.service.AdminAccessService;
import com.knds.service.AdminRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class AdminRoleServiceImpl implements AdminRoleService {

    private final AdminRoleRepository roleRepo;
    private final UserRepository userRepo;
    private final AdminAccessService accessService;

    public AdminRoleServiceImpl(AdminRoleRepository roleRepo,
                                UserRepository userRepo,
                                AdminAccessService accessService) {
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.accessService = accessService;
    }

    @Override
    @Transactional
    public AdminRoleResponse create(CreateAdminRoleRequest req, Long createdByUserId) {
        if (roleRepo.existsByName(req.name())) {
            throw new AdminRoleNameConflictException(req.name());
        }

        User creator = userRepo.findById(createdByUserId)
                .orElseThrow(() -> new UserNotFoundException(createdByUserId));

        AdminRole role = new AdminRole(req.name(), req.description(), creator);
        role.setPages(new HashSet<>(req.pages()));

        roleRepo.save(role);
        return toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminRoleResponse> listAll() {
        return roleRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminRoleResponse getById(Long id) {
        return toResponse(getRoleOrThrow(id));
    }

    @Override
    @Transactional
    public AdminRoleResponse update(Long id, UpdateAdminRoleRequest req) {
        AdminRole role = getRoleOrThrow(id);

        boolean pagesChanged = false;

        if (req.name() != null && !req.name().equals(role.getName())) {
            if (roleRepo.existsByName(req.name())) {
                throw new AdminRoleNameConflictException(req.name());
            }
            role.setName(req.name());
        }

        if (req.description() != null) {
            role.setDescription(req.description());
        }

        if (req.pages() != null && !req.pages().equals(role.getPages())) {
            role.setPages(new HashSet<>(req.pages()));
            pagesChanged = true;
        }

        // If pages changed, every user holding this role has stale cached page sets.
        // Cheapest correct option: nuke the whole cache.
        if (pagesChanged) {
            accessService.invalidateAll();
        }

        return toResponse(role);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AdminRole role = getRoleOrThrow(id);
        roleRepo.delete(role);

        // Role deletion cascades to admin_user_roles and admin_role_pages (FK ON DELETE CASCADE).
        // Every user previously holding this role now has fewer pages — nuke the cache.
        accessService.invalidateAll();
    }

    // ── helpers ────────────────────────────────────────────────────

    private AdminRole getRoleOrThrow(Long id) {
        return roleRepo.findById(id)
                .orElseThrow(() -> new AdminRoleNotFoundException(id));
    }

    private AdminRoleResponse toResponse(AdminRole r) {
        return new AdminRoleResponse(
                r.getId(),
                r.getName(),
                r.getDescription(),
                r.getPages(),
                r.getCreatedBy().getId(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}