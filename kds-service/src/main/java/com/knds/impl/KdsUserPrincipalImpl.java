package com.knds.impl;

import com.knds.entities.security.User;
import com.knds.service.KdsUserPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class KdsUserPrincipalImpl implements KdsUserPrincipal {

    private final Long   id;
    private final String email;
    private final String passwordHash;
    private final boolean enabled;
    private final List<GrantedAuthority> authorities;

    public KdsUserPrincipalImpl(User user) {
        this.id           = user.getId();
        this.email        = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.enabled      = user.isEnabled();
        this.authorities  = user.getRoles().stream()
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority(r.getName()))
                .toList();
    }

    public Long getId() { return id; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword()                  { return passwordHash; }
    @Override public String getUsername()                  { return email; }
    @Override public boolean isAccountNonExpired()         { return true; }
    @Override public boolean isAccountNonLocked()          { return true; }
    @Override public boolean isCredentialsNonExpired()     { return true; }
    @Override public boolean isEnabled()                   { return enabled; }
}