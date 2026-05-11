package com.knds.commons.exceptions;

public class InvitationNotFoundException extends RuntimeException {
    public InvitationNotFoundException(Long id) {
        super("Invitation not found: " + id);
    }
}