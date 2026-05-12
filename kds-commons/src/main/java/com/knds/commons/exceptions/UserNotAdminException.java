package com.knds.commons.exceptions;

public class UserNotAdminException extends RuntimeException {
    public UserNotAdminException(Long userId) {
        super("User is not an admin: " + userId);
    }
}