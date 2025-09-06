package org.example.authservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.authservice.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    // Inject from configuration; provide sensible default ONLY for dev.
    @Value("${jwt.secret:change_this_dev_secret_key_min_32_chars_123456789}")
    private String secret;

    @Value("${jwt.access.expiration.ms:900000}") // 15m default
    private long accessTokenExpiration;

    @Value("${jwt.refresh.expiration.ms:604800000}") // 7d default
    private long refreshTokenExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return baseBuilder(user)
                .claim("type", "access")
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .compact();
    }

    public String generateRefreshToken(User user) {
        return baseBuilder(user)
                .claim("type", "refresh")
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .compact();
    }

    private io.jsonwebtoken.JwtBuilder baseBuilder(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().toString())
                .setIssuedAt(new Date())
                .signWith(getSigningKey());
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }

    public boolean isTokenExpired(String token) {
        Date exp = extractClaim(token, Claims::getExpiration);
        return exp.before(new Date());
    }

    public String extractType(String token) {
        return extractClaim(token, c -> c.get("type", String.class));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractType(token));
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractType(token));
    }

    public boolean isTokenValid(String token, User user) {
        String email = extractEmail(token);
        return email.equals(user.getEmail()) && !isTokenExpired(token);
    }
    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpiration;
    }
}