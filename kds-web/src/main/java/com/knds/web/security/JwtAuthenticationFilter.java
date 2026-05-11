package com.knds.web.security;

import com.knds.service.JwtService;
import com.knds.service.security.AdminPageGuard;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(HEADER);

        // No token? Just continue. Public endpoints will work; protected ones will 401 later.
        if (header == null || !header.startsWith(PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIX.length());

        try {
            Claims claims = jwtService.parse(token);

            Long userId = Long.valueOf(claims.getSubject());
            String email = claims.get("email", String.class);

            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);

            var authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            var auth = new UsernamePasswordAuthenticationToken(
                    new JwtUserContext(userId, email),   // principal
                    null,                                 // credentials (none — JWT is the proof)
                    authorities
            );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException | IllegalArgumentException ex) {
            // Bad/expired/tampered token → leave context empty.
            // The AuthorizationFilter will return 401/403 for protected endpoints.
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }

    /** Lightweight principal — no DB hit per request. */
    public record JwtUserContext(Long userId, String email) implements AdminPageGuard.JwtPrincipal { }
}