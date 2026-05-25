package com.example.security.service;

import com.example.security.config.JwtProperties;
import com.example.security.dto.LoginRequest;
import com.example.security.dto.RefreshTokenRequest;
import com.example.security.dto.RegisterRequest;
import com.example.security.dto.TokenResponse;
import com.example.security.entity.AppUser;
import com.example.security.repository.AppUserRepository;
import com.example.security.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Transactional
    public void register(RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在: " + request.getUsername());
        }
        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole().toLowerCase())
                .createdAt(LocalDateTime.now())
                .build();
        appUserRepository.save(user);
    }

    public TokenResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
        } catch (AuthenticationException ex) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        AppUser user = appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return buildTokenResponse(user);
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)
                || !JwtTokenProvider.TOKEN_TYPE_REFRESH.equals(jwtTokenProvider.getTokenType(refreshToken))) {
            throw new IllegalArgumentException("无效的 refreshToken");
        }
        String username = jwtTokenProvider.getUsername(refreshToken);
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return buildTokenResponse(user);
    }

    private TokenResponse buildTokenResponse(AppUser user) {
        return TokenResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole()))
                .refreshToken(jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getRole()))
                .tokenType("Bearer")
                .accessTokenExpiresInMinutes(jwtProperties.getAccessTokenExpirationMinutes())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
