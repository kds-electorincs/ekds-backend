package com.knds.commons.exceptions;

public class InvalidInvitationTokenException extends RuntimeException {
    public InvalidInvitationTokenException(String reason) {
        super(reason);
    }
}