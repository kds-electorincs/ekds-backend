package com.knds.service;

import com.knds.commons.dto.UserProfileResponse;

public interface UserService {

    UserProfileResponse getProfile(Long userId);
}