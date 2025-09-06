package org.example.authservice.service;

import org.example.authservice.dto.AuthResponse;
import org.example.authservice.dto.LoginRequest;
import org.example.authservice.dto.RegisterRequest;
import org.example.authservice.dto.RefreshTokenRequest;
import org.example.authservice.dto.MessageResponse;
import org.example.authservice.exception.InvalidCredentialsException;
import org.example.authservice.exception.InvalidRefreshTokenException;
import org.example.authservice.exception.RefreshTokenExpiredException;
import org.example.authservice.exception.UserNotFoundException;
import org.example.authservice.exception.EmailAlreadyUsedException;
import org.example.authservice.model.Roles;
import org.example.authservice.model.User;
import org.example.authservice.model.RefreshToken;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.JwtException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    @Autowired
    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }
    @Transactional
    public MessageResponse register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyUsedException();
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(Roles.USER);
        userRepository.save(user);
        return new MessageResponse("User registered successfully");
    }
    public AuthResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail());
        if(user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new InvalidCredentialsException();
        }
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        String tokenHash = hashToken(refreshToken);
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtService.getRefreshTokenExpirationMs());
        RefreshToken entity = new RefreshToken(tokenHash, user, now, expiresAt);
        refreshTokenRepository.save(entity);
        return new AuthResponse(accessToken,refreshToken,user.getEmail(), user.getFullName(), user.getRole().toString());
    }

    public AuthResponse refresh(RefreshTokenRequest request){
        String refreshToken = request.getRefreshToken();
        if(refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token is missing");
        }
        try {
            if(!jwtService.isRefreshToken(refreshToken)) {
                throw new InvalidRefreshTokenException("Provided token is not a refresh token");
            }

            String tokenHash = hashToken(refreshToken);
            Optional<RefreshToken> maybe = refreshTokenRepository.findByTokenHash(tokenHash);
            if(maybe.isEmpty()){
                // token not found => unknown or already rotated/revoked
                throw new InvalidRefreshTokenException("Refresh token not found or revoked");
            }
            RefreshToken stored = maybe.get();
            if(stored.isRevoked()){
                throw new InvalidRefreshTokenException("Refresh token revoked");
            }
            Instant now = Instant.now();
            if(stored.getExpiresAt().isBefore(now)){
                throw new RefreshTokenExpiredException("Refresh token expired");
            }

            String email = jwtService.extractEmail(refreshToken);
            User user = stored.getUser();
            if(user == null || !user.getEmail().equals(email)){
                throw new UserNotFoundException("User for token no longer exists");
            }

            stored.setRevoked(true);
            refreshTokenRepository.save(stored);

            String newAccess = jwtService.generateAccessToken(user);
            String newRefresh = jwtService.generateRefreshToken(user);

            String newHash = hashToken(newRefresh);
            Instant expiresAt = now.plusMillis(jwtService.getRefreshTokenExpirationMs());
            RefreshToken newEntity = new RefreshToken(newHash, user, now, expiresAt);
            refreshTokenRepository.save(newEntity);

            return new AuthResponse(newAccess, newRefresh, user.getEmail(), user.getFullName(), user.getRole().toString());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidRefreshTokenException("Refresh token is invalid");
        }
    }

    private String hashToken(String token){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for(byte b : digest){
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }
}
