package org.example.project_java_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.project_java_service.model.dto.request.LoginRequest;
import org.example.project_java_service.model.dto.request.RegisterRequest;
import org.example.project_java_service.model.dto.response.TokenResponse;
import org.example.project_java_service.model.entity.enumeration.RoleEnum;
import org.example.project_java_service.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    // Test 1: Đăng nhập thành công
    @Test
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("ungvien1");
        request.setPassword("123456");

        TokenResponse mockResponse = TokenResponse.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .build();

        Mockito.when(authService.login(Mockito.any(LoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").value("mock-refresh-token"));
    }

    // Test 2: Đăng ký thành công
    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser", "password123", "newuser@gmail.com", RoleEnum.ROLE_CANDIDATE);

        Mockito.when(authService.register(Mockito.any(RegisterRequest.class))).thenReturn("Đăng ký tài khoản thành công!");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Đăng ký tài khoản thành công!"));
    }

    // Test 3: Đăng ký thất bại do Validation (Username ngắn)
    @Test
    void testRegister_Fail_ValidationError() throws Exception {
        RegisterRequest request = new RegisterRequest("ab", "password123", "email@gmail.com", RoleEnum.ROLE_CANDIDATE);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // Test 4: Đăng xuất thành công
    @Test
    void testLogout_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"some-token\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // Test 5: Đổi mật khẩu thành công
    @Test
    void testChangePassword_Success() throws Exception {
        org.springframework.security.core.Authentication mockAuth = Mockito.mock(org.springframework.security.core.Authentication.class);
        Mockito.when(mockAuth.getName()).thenReturn("nguoidung_test");

        Mockito.when(authService.changePassword(Mockito.any(), Mockito.anyString()))
                .thenReturn("Đổi mật khẩu thành công!");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/auth/change-password")
                        .principal(mockAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"123456\",\"newPassword\":\"matkhaumoi\",\"confirmPassword\":\"matkhaumoi\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}