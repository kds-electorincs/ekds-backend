package com.knds.web.controller;

import com.knds.commons.dto.AdminInvitationResponse;
import com.knds.commons.dto.CreateInvitationRequest;
import com.knds.web.security.JwtAuthenticationFilter;
import com.knds.service.AdminInvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/admin-management/invitations")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminInvitationController {

    private final AdminInvitationService invitationService;

    public AdminInvitationController(AdminInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminInvitationResponse create(
            @Valid @RequestBody CreateInvitationRequest req,
            @AuthenticationPrincipal JwtAuthenticationFilter.JwtUserContext principal) {
        return invitationService.create(req, principal.userId());
    }

    @GetMapping
    public List<AdminInvitationResponse> listAll() {
        return invitationService.listAll();
    }

    @PostMapping("/{id}/resend")
    public AdminInvitationResponse resend(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtAuthenticationFilter.JwtUserContext principal) {
        return invitationService.resend(id, principal.userId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtAuthenticationFilter.JwtUserContext principal) {
        invitationService.cancel(id, principal.userId());
    }
}