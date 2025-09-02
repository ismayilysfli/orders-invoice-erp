package org.example.authservice.service;

import org.example.authservice.dto.AuthResponse;
import org.example.authservice.dto.LoginRequest;
import org.example.authservice.dto.RegisterRequest;
import org.example.authservice.dto.RefreshTokenRequest;
import org.example.authservice.dto.MessageResponse;
import org.example.authservice.exception.LoginFailedException;
import org.example.authservice.exception.InvalidRefreshTokenException;
import org.example.authservice.exception.EmailAlreadyUsedException;
import org.example.authservice.model.Roles;
import org.example.authservice.model.User;
import org.example.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.JwtException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
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
            throw new LoginFailedException();
        }
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
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
            if(jwtService.isTokenExpired(refreshToken)) {
                throw new InvalidRefreshTokenException("Refresh token expired");
            }
            String email = jwtService.extractEmail(refreshToken);
            User user = userRepository.findByEmail(email);
            if(user == null) {
                throw new InvalidRefreshTokenException("User for token no longer exists");
            }
            String newAccess = jwtService.generateAccessToken(user);
            String newRefresh = jwtService.generateRefreshToken(user);
            return new AuthResponse(newAccess, newRefresh, user.getEmail(), user.getFullName(), user.getRole().toString());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidRefreshTokenException("Refresh token is invalid");
        }
    }
}
