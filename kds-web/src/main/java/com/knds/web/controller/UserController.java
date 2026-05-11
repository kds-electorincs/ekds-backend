package com.knds.web.controller;

import com.knds.commons.dto.UserProfileResponse;
import com.knds.web.security.JwtAuthenticationFilter;
import com.knds.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserProfileResponse me(@AuthenticationPrincipal JwtAuthenticationFilter.JwtUserContext principal) {
        return userService.getProfile(principal.userId());
    }
}