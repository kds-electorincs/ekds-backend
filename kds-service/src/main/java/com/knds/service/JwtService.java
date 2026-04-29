package com.knds.service;

import io.jsonwebtoken.Claims;

import java.util.List;

public interface JwtService {
    public String issueAccessToken(Long userId, String email, List<String> roles);
    public Claims parse(String token);
}
