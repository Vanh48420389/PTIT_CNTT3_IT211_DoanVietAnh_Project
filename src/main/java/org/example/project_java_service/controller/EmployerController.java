package org.example.project_java_service.controller;

import org.example.project_java_service.model.dto.request.JobPostingRequest;
import org.example.project_java_service.model.dto.response.ApplicationResponse;
import org.example.project_java_service.model.dto.response.JobPostingResponse;
import org.example.project_java_service.model.entity.enumeration.ApplicationStatusEnum;
import org.example.project_java_service.service.EmployerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employer/jobs")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
public class EmployerController {

    private final EmployerService employerService;

    // 1. Tạo tin tuyển dụng mới
    @PostMapping
    public ResponseEntity<String> createJob(
            @Valid @RequestBody JobPostingRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(employerService.createJob(request, authentication.getName()));
    }

    // 2. Lấy danh sách tin đã đăng của bản thân
    @GetMapping
    public ResponseEntity<Page<JobPostingResponse>> getMyJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        return ResponseEntity.ok(employerService.getMyJobs(authentication.getName(), page, size));
    }

    // 3. Sửa tin
    @PutMapping("/{id}")
    public ResponseEntity<String> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobPostingRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(employerService.updateJob(id, request, authentication.getName()));
    }

    // 4. Xóa tin
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJob(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(employerService.deleteJob(id, authentication.getName()));
    }

    // 5. Xem danh sách hồ sơ của 1 bài đăng cụ thể
    @GetMapping("/{jobId}/applications")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsForJob(
            @PathVariable Long jobId,
            Authentication authentication) {
        return ResponseEntity.ok(employerService.getApplicationsForJob(jobId, authentication.getName()));
    }

    // 6. Cập nhật trạng thái của 1 hồ sơ cụ thể
    @PutMapping("/applications/{applicationId}/status")
    public ResponseEntity<String> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestParam ApplicationStatusEnum status,
            Authentication authentication) {
        return ResponseEntity.ok(employerService.updateApplicationStatus(applicationId, status, authentication.getName()));
    }
}