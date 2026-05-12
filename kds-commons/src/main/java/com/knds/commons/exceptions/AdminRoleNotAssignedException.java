package com.knds.commons.exceptions;

public class AdminRoleNotAssignedException extends RuntimeException {
    public AdminRoleNotAssignedException(Long userId, Long roleId) {
        super("User " + userId + " does not have admin role " + roleId);
    }
}