package com.knds.web.controller;

import com.knds.commons.dto.AcceptInvitationRequest;
import com.knds.commons.dto.AuthTokens;
import com.knds.commons.dto.InvitationPreviewResponse;
import com.knds.commons.dto.LoginRequest;
import com.knds.commons.dto.RefreshRequest;
import com.knds.commons.dto.RegisterRequest;
import com.knds.service.AdminInvitationService;
import com.knds.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AdminInvitationService invitationService;

    public AuthController(AuthService authService, AdminInvitationService invitationService) {
        this.authService = authService;
        this.invitationService = invitationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthTokens register(@Valid @RequestBody RegisterRequest req,
                               HttpServletRequest http) {
        return authService.register(req, http.getHeader("User-Agent"), clientIp(http));
    }

    @PostMapping("/login")
    public AuthTokens login(@Valid @RequestBody LoginRequest req,
                            HttpServletRequest http) {
        return authService.login(req, http.getHeader("User-Agent"), clientIp(http));
    }

    @PostMapping("/refresh")
    public AuthTokens refresh(@Valid @RequestBody RefreshRequest req,
                              HttpServletRequest http) {
        return authService.refresh(req.refreshToken(), http.getHeader("User-Agent"), clientIp(http));
    }

    /** Requires a valid access token (proves identity) AND a refresh token (says which session to kill). */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void logout(@Valid @RequestBody RefreshRequest req) {
        authService.logout(req.refreshToken());
    }

    private static String clientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
    @GetMapping("/invitations/{token}")
    public InvitationPreviewResponse previewInvitation(@PathVariable String token) {
        return invitationService.preview(token);
    }

    @PostMapping("/invitations/{token}/accept")
    public AuthTokens acceptInvitation(
            @PathVariable String token,
            @Valid @RequestBody AcceptInvitationRequest req,
            HttpServletRequest http) {
        return invitationService.accept(
                token,
                req,
                http.getHeader("User-Agent"),
                clientIp(http)
        );
    }
}