package com.knds.impl;

import com.knds.repository.security.UserRepository;
import com.knds.service.KdsUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class KdsUserDetailsServiceImpl implements KdsUserDetailsService {

    private final UserRepository userRepository;

    public KdsUserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailIgnoreCase(email)
                .map(KdsUserPrincipalImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}