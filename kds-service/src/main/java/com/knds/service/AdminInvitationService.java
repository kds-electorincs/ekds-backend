package com.knds.service;

import com.knds.commons.dto.AcceptInvitationRequest;
import com.knds.commons.dto.AdminInvitationResponse;
import com.knds.commons.dto.AuthTokens;
import com.knds.commons.dto.CreateInvitationRequest;
import com.knds.commons.dto.InvitationPreviewResponse;

import java.util.List;

public interface AdminInvitationService {

    AdminInvitationResponse create(CreateInvitationRequest request, Long createdByUserId);

    List<AdminInvitationResponse> listAll();

    AdminInvitationResponse resend(Long invitationId, Long requestedByUserId);

    void cancel(Long invitationId, Long cancelledByUserId);

    InvitationPreviewResponse preview(String rawToken);

    AuthTokens accept(String rawToken, AcceptInvitationRequest request,
                      String userAgent, String ipAddress);
}
