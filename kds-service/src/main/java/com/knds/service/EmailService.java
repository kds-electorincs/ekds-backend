package com.knds.service;

public interface EmailService {

    /** Send the admin invitation email with the click-through link. */
    void sendAdminInvitation(String toEmail, String acceptUrl, String invitedByName);
}