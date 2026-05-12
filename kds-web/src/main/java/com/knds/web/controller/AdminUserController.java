package com.knds.web.controller;

import com.knds.commons.dto.AdminUserResponse;
import com.knds.web.security.JwtAuthenticationFilter;
import com.knds.service.AdminUserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/admin-management/admins")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<AdminUserResponse> listAdmins() {
        return adminUserService.listAdmins();
    }

    @GetMapping("/{userId}")
    public AdminUserResponse getAdmin(@PathVariable Long userId) {
        return adminUserService.getAdmin(userId);
    }

    @PostMapping("/{userId}/promote")
    public AdminUserResponse promote(@PathVariable Long userId) {
        return adminUserService.promoteToAdmin(userId);
    }

    @PostMapping("/{userId}/demote")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void demote(@PathVariable Long userId) {
        adminUserService.demoteFromAdmin(userId);
    }

    @PostMapping("/{userId}/roles/{roleId}")
    public AdminUserResponse assignRole(
            @PathVariable Long userId,
            @PathVariable Long roleId,
            @AuthenticationPrincipal JwtAuthenticationFilter.JwtUserContext principal) {
        return adminUserService.assignRole(userId, roleId, principal.userId());
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeRole(@PathVariable Long userId, @PathVariable Long roleId) {
        adminUserService.revokeRole(userId, roleId);
    }
}