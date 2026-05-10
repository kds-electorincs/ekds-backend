package com.knds.commons.exceptions;

public class AdminRoleNotFoundException extends RuntimeException {
    public AdminRoleNotFoundException(Long id) {
        super("Admin role not found: " + id);
    }
}