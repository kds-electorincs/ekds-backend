package com.knds.service.impl;

import com.knds.commons.dto.AuthTokens;
import com.knds.commons.dto.LoginRequest;
import com.knds.commons.dto.RegisterRequest;
import com.knds.commons.exceptions.EmailAlreadyRegisteredException;
import com.knds.commons.exceptions.InvalidCredentialsException;
import com.knds.entities.security.Role;
import com.knds.entities.security.User;
import com.knds.repository.security.RoleRepository;
import com.knds.repository.security.UserRepository;
import com.knds.service.security.JwtProperties;
import com.knds.service.AuthService;
import com.knds.service.JwtService;
import com.knds.service.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProps;

    public AuthServiceImpl(UserRepository userRepo,
                           RoleRepository roleRepo,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           RefreshTokenService refreshTokenService,
                           JwtProperties jwtProps) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.jwtProps = jwtProps;
    }

    @Override
    @Transactional
    public AuthTokens register(RegisterRequest req, String userAgent, String ipAddress) {
        if (userRepo.existsByEmailIgnoreCase(req.email())) {
            throw new EmailAlreadyRegisteredException(req.email());
        }

        Role userRole = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not seeded — check V002"));

        User user = new User(
                req.email().toLowerCase(),
                passwordEncoder.encode(req.password()),
                req.fullName()
        );
        user.setPhone(req.phone());
        Set<Role> roles = user.getRoles();
        roles.add(userRole);
        user.setRoles(roles);
        userRepo.save(user);

        return mintTokenPair(user, userAgent, ipAddress);
    }

    @Override
    @Transactional
    public AuthTokens login(LoginRequest req, String userAgent, String ipAddress) {
        User user = userRepo.findByEmailIgnoreCase(req.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return mintTokenPair(user, userAgent, ipAddress);
    }

    @Override
    @Transactional
    public AuthTokens refresh(String refreshToken, String userAgent, String ipAddress) {
        RefreshTokenService.IssuedRefreshToken rotated = refreshTokenService.rotate(refreshToken, userAgent, ipAddress);
        User user = rotated.entity().getUser();
        return buildTokenResponse(user, rotated);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    // ── helpers ────────────────────────────────────────────────────

    private AuthTokens mintTokenPair(User user, String userAgent, String ipAddress) {
        RefreshTokenService.IssuedRefreshToken rt = refreshTokenService.issue(user, userAgent, ipAddress);
        return buildTokenResponse(user, rt);
    }

    private AuthTokens buildTokenResponse(User user, RefreshTokenService.IssuedRefreshToken rt) {
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        String accessToken = jwtService.issueAccessToken(user.getId(), user.getEmail(), roles);
        OffsetDateTime accessExpiry = OffsetDateTime.now().plus(jwtProps.accessTokenTtl());

        return new AuthTokens(
                accessToken,
                rt.rawToken(),
                accessExpiry,
                rt.entity().getExpiresAt()
        );
    }
}