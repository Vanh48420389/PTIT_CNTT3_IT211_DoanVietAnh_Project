package org.example.project_java_service.controller;

import org.example.project_java_service.model.dto.response.JobPostingResponse;
import org.example.project_java_service.model.entity.enumeration.JobStatusEnum;
import org.example.project_java_service.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private final AdminService adminService;

    @GetMapping("/jobs")
    public ResponseEntity<Page<JobPostingResponse>> getApprovedJobs(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminService.searchJobs(keyword, JobStatusEnum.APPROVED, page, size, "id", "desc"));
    }
}