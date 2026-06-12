package org.example.project_java_service.controller;

import org.example.project_java_service.service.EmployerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class EmployerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployerService employerService;

    private UsernamePasswordAuthenticationToken authToken;
    private String validJsonRequest;

    @BeforeEach
    void setUp() {
        // 1. Cấp quyền ROLE_EMPLOYER trực tiếp vào bộ nhớ để vượt qua @PreAuthorize
        authToken = new UsernamePasswordAuthenticationToken(
                "employer_test",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 2. Tạo chuỗi JSON hợp lệ để vượt qua màng lọc @Valid của DTO
        validJsonRequest = "{\"title\":\"Tuyển Lập trình viên Java\",\"description\":\"Biết Spring Boot\",\"salaryRange\":\"10-20 Triệu\"}";
    }

    // Test 1: Tạo tin tuyển dụng
    @Test
    void testCreateJob_Success() throws Exception {
        Mockito.when(employerService.createJob(Mockito.any(), Mockito.eq("employer_test")))
                .thenReturn("Đăng tin tuyển dụng thành công. Tin của bạn đang chờ Admin phê duyệt!");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/employer/jobs")
                        .principal(authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJsonRequest)) // Truyền JSON hợp lệ vào đây
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // Test 2: Lấy danh sách tin
    @Test
    void testGetMyJobs_Success() throws Exception {
        Mockito.when(employerService.getMyJobs(Mockito.eq("employer_test"), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/employer/jobs")
                        .principal(authToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // Test 3: Sửa tin
    @Test
    void testUpdateJob_Success() throws Exception {
        Mockito.when(employerService.updateJob(Mockito.eq(1L), Mockito.any(), Mockito.eq("employer_test")))
                .thenReturn("Cập nhật thành công. Tin đang được đưa về trạng thái chờ duyệt lại!");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/employer/jobs/1")
                        .principal(authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJsonRequest)) // Truyền JSON hợp lệ vào đây
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // Test 4: Xóa tin
    @Test
    void testDeleteJob_Success() throws Exception {
        Mockito.when(employerService.deleteJob(Mockito.eq(1L), Mockito.eq("employer_test")))
                .thenReturn("Đã xóa tin tuyển dụng thành công!");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/employer/jobs/1")
                        .principal(authToken))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // Test 5: Cập nhật trạng thái hồ sơ
    @Test
    void testUpdateApplicationStatus_Success() throws Exception {
        Mockito.when(employerService.updateApplicationStatus(Mockito.eq(10L), Mockito.any(), Mockito.eq("employer_test")))
                .thenReturn("Đã cập nhật trạng thái hồ sơ thành: PENDING");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/employer/jobs/applications/10/status")
                        .principal(authToken)
                        .param("status", "PENDING")) // Fix cứng 1 giá trị enum hợp lệ
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}