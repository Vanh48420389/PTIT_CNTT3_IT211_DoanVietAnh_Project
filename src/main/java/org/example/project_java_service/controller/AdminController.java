package org.example.project_java_service.controller;

import org.example.project_java_service.model.dto.response.JobPostingResponse;
import org.example.project_java_service.model.dto.response.UserResponse;
import org.example.project_java_service.model.entity.enumeration.JobStatusEnum;
import org.example.project_java_service.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(adminService.searchUsers(keyword, page, size, sortBy, sortDir));
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleUserStatus(id));
    }


    // 1. Xem danh sách tin
    @GetMapping("/jobs")
    public ResponseEntity<Page<JobPostingResponse>> searchJobs(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) JobStatusEnum status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(adminService.searchJobs(keyword, status, page, size, sortBy, sortDir));
    }

    // 2. Cập nhật trạng thái duyệt tin
    @PutMapping("/jobs/{id}/review")
    public ResponseEntity<String> reviewJob(
            @PathVariable Long id,
            @RequestParam JobStatusEnum status
    ) {
        return ResponseEntity.ok(adminService.reviewJob(id, status));
    }
}