package org.example.project_java_service;

import org.example.project_java_service.model.dto.request.ForgotPasswordRequest;
import org.example.project_java_service.model.dto.request.RegisterRequest;
import org.example.project_java_service.model.entity.User;
import org.example.project_java_service.model.entity.enumeration.RoleEnum;
import org.example.project_java_service.repository.UserRepository;
import org.example.project_java_service.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private AuthServiceImpl authService;

    // Test 1: Đăng ký thành công
    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest("ungvien1", "123", "ungvien1@gmail.com", RoleEnum.ROLE_CANDIDATE);

        Mockito.when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        Mockito.when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        Mockito.when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        String response = authService.register(request);

        Assertions.assertEquals("Đăng ký tài khoản thành công!", response);
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }

    // Test 2: Đăng ký thất bại do Username đã tồn tại
    @Test
    void testRegister_Fail_UsernameExists() {
        RegisterRequest request = new RegisterRequest("ungvien1", "123", "ungvien1@gmail.com", RoleEnum.ROLE_CANDIDATE);
        Mockito.when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> authService.register(request));
        Assertions.assertEquals("Username đã được sử dụng!", exception.getMessage());
    }

    // Test 3: Đăng ký thất bại do quyền ADMIN
    @Test
    void testRegister_Fail_RoleAdmin() {
        RegisterRequest request = new RegisterRequest("admin_fake", "123", "admin@gmail.com", RoleEnum.ROLE_ADMIN);
        Mockito.when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        Mockito.when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> authService.register(request));
        Assertions.assertEquals("Không thể đăng ký tài khoản quyền quản trị!", exception.getMessage());
    }

    // Test 4: Quên mật khẩu thành công
    @Test
    void testForgotPassword_Success() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("user@gmail.com");
        User mockUser = User.builder().username("user").email("user@gmail.com").build();

        Mockito.when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("newEncodedPass");

        String response = authService.forgotPassword(request);

        Assertions.assertTrue(response.contains("Mật khẩu mới đã được gửi"));
        Mockito.verify(userRepository, Mockito.times(1)).save(mockUser);
        Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.any(org.springframework.mail.SimpleMailMessage.class));
    }

    // Test 5: Quên mật khẩu thất bại (Không tìm thấy email)
    @Test
    void testForgotPassword_Fail_EmailNotFound() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("notfound@gmail.com");

        Mockito.when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> authService.forgotPassword(request));
        Assertions.assertEquals("Không tìm thấy tài khoản nào đăng ký với email này!", exception.getMessage());
    }
}