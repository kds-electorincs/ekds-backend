package com.knds.commons.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateInvitationRequest(
        @Email @NotBlank @Size(max = 255)
        String email,

        @NotEmpty
        Set<Long> roleIds
) { }