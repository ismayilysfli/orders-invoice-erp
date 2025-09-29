package com.example.apigateway.security;

import com.example.apigateway.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Component
public class JwtValidator {
    private static final Logger log = LoggerFactory.getLogger(JwtValidator.class);
    private final JwtProperties props;
    private final SecretKey key;

    public JwtValidator(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public JwtPrincipal validate(String token) throws JwtException {
        if (token == null || token.isBlank()) {
            throw new JwtException("Empty token");
        }
        JwtParserBuilder builder = Jwts.parserBuilder().setSigningKey(key).setAllowedClockSkewSeconds(props.getAccessClockSkewSeconds());
        Jws<Claims> jws = builder.build().parseClaimsJws(token);
        Claims claims = jws.getBody();
        if (props.getExpectedIssuer() != null && !props.getExpectedIssuer().isBlank()) {
            String iss = claims.getIssuer();
            if (iss == null || !iss.equals(props.getExpectedIssuer())) {
                throw new JwtException("Invalid issuer");
            }
        }
        Instant now = Instant.now();
        Date exp = claims.getExpiration();
        if (exp != null && exp.toInstant().isBefore(now.minusSeconds(props.getAccessClockSkewSeconds()))) {
            throw new JwtException("Token expired");
        }
        String subject = claims.getSubject();
        String userId = claims.get("userId", String.class);
        Set<String> roles = extractRoles(claims);
        return new JwtPrincipal(userId, subject, roles, claims);
    }

    private Set<String> extractRoles(Claims claims) {
        Object raw = claims.get(props.getRoleClaim());
        if (raw == null) return Collections.emptySet();
        if (raw instanceof Collection<?> c) {
            Set<String> out = new LinkedHashSet<>();
            for (Object o : c) out.add(String.valueOf(o));
            return out;
        }
        if (raw instanceof String s) {
            if (s.contains(",")) {
                String[] parts = s.split(",");
                Set<String> out = new LinkedHashSet<>();
                for (String p : parts) out.add(p.trim());
                return out;
            } else {
                return Set.of(s.trim());
            }
        }
        return Set.of(raw.toString());
    }
}

