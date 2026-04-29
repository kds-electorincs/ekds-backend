package com.knds.impl;

import com.knds.security.JwtProperties;
import com.knds.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
@Service
public class JwtServiceImpl implements JwtService {

    private final JwtProperties props;
    private final SecretKey signingKey;

    public JwtServiceImpl(JwtProperties props) {
        this.props = props;
        this.signingKey = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    /** Build an access token for a logged-in user. */
    public String issueAccessToken(Long userId, String email, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())          // jti — unique token id
                .issuer(props.issuer())
                .subject(String.valueOf(userId))            // sub — user id
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(props.accessTokenTtl())))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /** Parse + verify. Throws JwtException subclasses on any failure (expired, bad signature, malformed). */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(props.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}