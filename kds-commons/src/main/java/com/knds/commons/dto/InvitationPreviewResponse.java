package com.knds.commons.dto;

import java.util.List;

public record InvitationPreviewResponse(
        String email,
        List<String> roleNames
) { }