package org.example.project_java_service.service.impl;

import org.example.project_java_service.model.dto.request.LoginRequest;
import org.example.project_java_service.model.dto.request.LogoutRequest;
import org.example.project_java_service.model.dto.request.RefreshTokenRequest;
import org.example.project_java_service.model.dto.request.RegisterRequest;
import org.example.project_java_service.model.dto.request.ChangePasswordRequest;
import org.example.project_java_service.model.dto.request.ForgotPasswordRequest;
import org.example.project_java_service.model.dto.response.TokenResponse;
import org.example.project_java_service.model.entity.RefreshToken;
import org.example.project_java_service.model.entity.User;
import org.example.project_java_service.model.entity.enumeration.RoleEnum;
import org.example.project_java_service.repository.RefreshTokenRepository;
import org.example.project_java_service.repository.UserRepository;
import org.example.project_java_service.security.user.CustomUserDetails;
import org.example.project_java_service.security.jwt.JwtUtils;
import org.example.project_java_service.service.AuthService;
import org.example.project_java_service.service.RedisTokenBlacklistService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Đã thêm thư viện Log
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    private final RedisTokenBlacklistService redisTokenBlacklistService;
    private final HttpServletRequest httpServletRequest;

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
                        refreshTokenRepository.delete(refreshToken);
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
                            .refreshToken(requestRefreshToken)
                            .build();
                })
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại trong hệ thống!"));
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(refreshTokenRepository::delete);

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            try {
                long expirationTime = jwtUtils.extractExpiration(accessToken).getTime() - System.currentTimeMillis();
                if (expirationTime > 0) {
                    redisTokenBlacklistService.addToBlacklist(accessToken, expirationTime);
                }
            } catch (Exception e) {
                log.warn("Lỗi khi xử lý Access Token lúc Logout (Có thể Token đã hết hạn sẵn): {}", e.getMessage());
            }
        }
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


    @Override
    @Transactional
    public String changePassword(ChangePasswordRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return "Đổi mật khẩu thành công! Lần đăng nhập sau hãy dùng mật khẩu mới.";
    }

    @Override
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản nào đăng ký với email này!"));

        String newRandomPassword = UUID.randomUUID().toString().substring(0, 8);

        user.setPassword(passwordEncoder.encode(newRandomPassword));
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("[Hệ thống Tuyển Dụng] Yêu cầu cấp lại mật khẩu");
        message.setText("Xin chào " + user.getUsername() + ",\n\n"
                + "Bạn vừa yêu cầu cấp lại mật khẩu cho tài khoản của mình.\n"
                + "Mật khẩu mới của bạn là: " + newRandomPassword + "\n\n"
                + "Vui lòng đăng nhập và tiến hành đổi lại mật khẩu ngay lập tức để đảm bảo an toàn cho tài khoản.\n\n"
                + "Trân trọng,\nĐội ngũ quản trị.");

        mailSender.send(message);

        return "Mật khẩu mới đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư (bao gồm cả mục Spam) !";
    }
}