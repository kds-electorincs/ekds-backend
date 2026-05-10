package com.knds.controller;

import com.knds.commons.dto.AdminRoleResponse;
import com.knds.commons.dto.CreateAdminRoleRequest;
import com.knds.commons.dto.UpdateAdminRoleRequest;
import com.knds.security.JwtAuthenticationFilter;
import com.knds.service.AdminRoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/admin-management/roles")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminRoleController {

    private final AdminRoleService roleService;

    public AdminRoleController(AdminRoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminRoleResponse create(
            @Valid @RequestBody CreateAdminRoleRequest req,
            @AuthenticationPrincipal JwtAuthenticationFilter.JwtUserContext principal) {
        return roleService.create(req, principal.userId());
    }

    @GetMapping
    public List<AdminRoleResponse> listAll() {
        return roleService.listAll();
    }

    @GetMapping("/{id}")
    public AdminRoleResponse getById(@PathVariable Long id) {
        return roleService.getById(id);
    }

    @PatchMapping("/{id}")
    public AdminRoleResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAdminRoleRequest req) {
        return roleService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }
}