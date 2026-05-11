package com.knds.commons.exceptions;

public class InvitationNotPendingException extends RuntimeException {
    public InvitationNotPendingException(Long id, String currentStatus) {
        super("Invitation " + id + " is " + currentStatus + ", expected PENDING");
    }
}