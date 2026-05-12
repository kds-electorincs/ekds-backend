package com.knds.service;

import com.knds.commons.dto.AdminUserResponse;

import java.util.List;

public interface AdminUserService {

    /** All users currently holding ROLE_ADMIN, with their assigned admin roles. */
    List<AdminUserResponse> listAdmins();

    /** Single admin user view. Throws UserNotAdminException if the user isn't an admin. */
    AdminUserResponse getAdmin(Long userId);

    /** Grants ROLE_ADMIN to a regular user. No-op if they're already an admin. */
    AdminUserResponse promoteToAdmin(Long userId);

    /**
     * Removes ROLE_ADMIN and all admin role assignments.
     * Cannot demote anyone holding ROLE_SUPER_ADMIN.
     */
    void demoteFromAdmin(Long userId);

    /** Assigns a custom admin role to an admin user. */
    AdminUserResponse assignRole(Long userId, Long roleId, Long grantedByUserId);

    /** Revokes a custom admin role from an admin user. */
    AdminUserResponse revokeRole(Long userId, Long roleId);
}