package com.knds.impl;

import com.knds.commons.dto.UserProfileResponse;
import com.knds.commons.exceptions.UserNotFoundException;
import com.knds.entities.security.User;
import com.knds.entities.security.Role;
import com.knds.repository.security.UserRepository;
import com.knds.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;

    public UserServiceImpl(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.isEmailVerified(),
                roles,
                user.getCreatedAt()
        );
    }
}