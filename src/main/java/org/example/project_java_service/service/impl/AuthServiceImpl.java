package org.example.project_java_service.service.impl;

import org.example.project_java_service.model.dto.request.LoginRequest;
import org.example.project_java_service.model.dto.request.LogoutRequest;
import org.example.project_java_service.model.dto.request.RefreshTokenRequest;
import org.example.project_java_service.model.dto.request.RegisterRequest;
import org.example.project_java_service.model.dto.response.TokenResponse;
import org.example.project_java_service.model.entity.RefreshToken;
import org.example.project_java_service.model.entity.User;
import org.example.project_java_service.model.entity.enumeration.RoleEnum;
import org.example.project_java_service.repository.RefreshTokenRepository;
import org.example.project_java_service.repository.UserRepository;
import org.example.project_java_service.security.user.CustomUserDetails;
import org.example.project_java_service.security.jwt.JwtUtils;
import org.example.project_java_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshTokenDurationMs;

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String accessToken = jwtUtils.generateAccessToken(userDetails);
        RefreshToken refreshToken = createRefreshToken(user);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã được sử dụng!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }
        if (request.getRole() == RoleEnum.ROLE_ADMIN) {
            throw new RuntimeException("Không thể đăng ký tài khoản quyền quản trị!");
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(request.getRole())
                .isActive(true)
                .build();

        userRepository.save(newUser);
        return "Đăng ký tài khoản thành công!";
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(refreshToken -> {
                    if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
                        refreshTokenRepository.delete(refreshToken); // Xóa luôn cho nhẹ DB
                        throw new RuntimeException("Refresh token đã hết hạn. Vui lòng đăng nhập lại!");
                    }
                    if (refreshToken.isRevoked()) {
                        throw new RuntimeException("Refresh token đã bị thu hồi!");
                    }
                    return refreshToken;
                })
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = jwtUtils.generateAccessToken(new CustomUserDetails(user));

                    return TokenResponse.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(requestRefreshToken) // Giữ nguyên Refresh Token cũ
                            .build();
                })
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại trong hệ thống!"));
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(refreshTokenRepository::delete);
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }
}