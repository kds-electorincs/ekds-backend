package com.knds.commons.exceptions;

public class PendingInvitationExistsException extends RuntimeException {
    public PendingInvitationExistsException(String email) {
        super("A pending invitation already exists for: " + email);
    }
}