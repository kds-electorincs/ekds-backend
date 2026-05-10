package com.knds.commons.dto;
import com.knds.commons.security.AdminPage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateAdminRoleRequest(
        @NotBlank @Size(min = 2, max = 64)
        String name,

        @Size(max = 255)
        String description,

        @NotEmpty
        Set<AdminPage> pages
) { }