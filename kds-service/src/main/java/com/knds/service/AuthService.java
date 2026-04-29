package com.knds.service;

import com.knds.commons.dto.AuthTokens;
import com.knds.commons.dto.LoginRequest;
import com.knds.commons.dto.RegisterRequest;

public interface AuthService {

    AuthTokens register(RegisterRequest request, String userAgent, String ipAddress);

    AuthTokens login(LoginRequest request, String userAgent, String ipAddress);

    AuthTokens refresh(String refreshToken, String userAgent, String ipAddress);

    void logout(String refreshToken);
}