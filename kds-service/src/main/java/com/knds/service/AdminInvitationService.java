package com.knds.service;

import com.knds.commons.dto.AdminInvitationResponse;
import com.knds.commons.dto.CreateInvitationRequest;

import java.util.List;

public interface AdminInvitationService {

    AdminInvitationResponse create(CreateInvitationRequest request, Long createdByUserId);

    List<AdminInvitationResponse> listAll();

    AdminInvitationResponse resend(Long invitationId, Long requestedByUserId);

    void cancel(Long invitationId, Long cancelledByUserId);
}
