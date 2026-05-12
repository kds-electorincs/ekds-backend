package com.knds.web.error;

import com.knds.commons.exceptions.AdminRoleNameConflictException;
import com.knds.commons.exceptions.AdminRoleNotFoundException;
import com.knds.commons.exceptions.EmailAlreadyRegisteredException;
import com.knds.commons.exceptions.InvalidCredentialsException;
import com.knds.commons.exceptions.InvalidInvitationTokenException;
import com.knds.commons.exceptions.InvalidRefreshTokenException;
import com.knds.commons.exceptions.InvitationNotFoundException;
import com.knds.commons.exceptions.InvitationNotPendingException;
import com.knds.commons.exceptions.PendingInvitationExistsException;
import com.knds.commons.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ProblemDetail handleEmailExists(EmailAlreadyRegisteredException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Email already registered");
        return pd;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleBadCredentials(InvalidCredentialsException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        pd.setTitle("Authentication failed");
        return pd;
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ProblemDetail handleBadRefresh(InvalidRefreshTokenException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        pd.setTitle("Invalid refresh token");
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied");
        pd.setTitle("Forbidden");
        return pd;
    }

    /** Bean Validation failures from @Valid. Returns field-level error map. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.put(fe.getField(), fe.getDefaultMessage()));

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setTitle("Validation failed");
        pd.setProperty("errors", fieldErrors);
        return pd;
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("User not found");
        return pd;
    }

    @ExceptionHandler(AdminRoleNotFoundException.class)
    public ProblemDetail handleAdminRoleNotFound(AdminRoleNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Admin role not found");
        return pd;
    }

    @ExceptionHandler(AdminRoleNameConflictException.class)
    public ProblemDetail handleAdminRoleNameConflict(AdminRoleNameConflictException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Admin role name conflict");
        return pd;
    }
    @ExceptionHandler(InvitationNotFoundException.class)
    public ProblemDetail handleInvitationNotFound(InvitationNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Invitation not found");
        return pd;
    }

    @ExceptionHandler(InvitationNotPendingException.class)
    public ProblemDetail handleInvitationNotPending(InvitationNotPendingException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Invitation is no longer pending");
        return pd;
    }

    @ExceptionHandler(PendingInvitationExistsException.class)
    public ProblemDetail handlePendingInvitationExists(PendingInvitationExistsException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Pending invitation exists");
        return pd;
    }
    @ExceptionHandler(InvalidInvitationTokenException.class)
    public ProblemDetail handleInvalidInvitationToken(InvalidInvitationTokenException ex) {
        // Note: ex.getMessage() is for server logs. The detail in the response
        // is intentionally generic to avoid information disclosure.
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.GONE,
                "This invitation link is invalid or expired"
        );
        pd.setTitle("Invitation unavailable");
        return pd;
    }
}