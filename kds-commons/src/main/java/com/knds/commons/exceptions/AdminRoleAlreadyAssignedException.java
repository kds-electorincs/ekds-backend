package com.knds.commons.exceptions;

public class AdminRoleAlreadyAssignedException extends RuntimeException {
    public AdminRoleAlreadyAssignedException(Long userId, Long roleId) {
        super("User " + userId + " already has admin role " + roleId);
    }
}