package org.example.project_java_service.service;

import org.example.project_java_service.model.dto.request.LoginRequest;
import org.example.project_java_service.model.dto.request.LogoutRequest;
import org.example.project_java_service.model.dto.request.RefreshTokenRequest;
import org.example.project_java_service.model.dto.request.RegisterRequest;
import org.example.project_java_service.model.dto.response.TokenResponse;

public interface AuthService {

    TokenResponse login(LoginRequest request);

    String register(RegisterRequest request);

    TokenResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);
}