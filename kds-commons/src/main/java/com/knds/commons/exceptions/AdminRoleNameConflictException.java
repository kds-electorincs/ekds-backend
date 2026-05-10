package com.knds.commons.exceptions;

public class AdminRoleNameConflictException extends RuntimeException {
    public AdminRoleNameConflictException(String name) {
        super("Admin role already exists with name: " + name);
    }
}