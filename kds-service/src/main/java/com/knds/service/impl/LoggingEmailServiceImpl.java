package com.knds.service.impl;

import com.knds.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingEmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailServiceImpl.class);

    @Override
    public void sendAdminInvitation(String toEmail, String acceptUrl, String invitedByName) {
        log.info("""
            
            ================================================================
            [EMAIL STUB] Admin Invitation
            ================================================================
            To:           {}
            Invited by:   {}
            Accept link:  {}
            ================================================================
            (Real SMTP wiring deferred to Phase 5. Copy the link manually.)
            
            """, toEmail, invitedByName, acceptUrl);
    }
}