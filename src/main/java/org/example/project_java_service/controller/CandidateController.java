package org.example.project_java_service.controller;

import org.example.project_java_service.model.dto.request.ApplyJobRequest;
import org.example.project_java_service.service.CandidateService;
import org.example.project_java_service.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/candidate")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_CANDIDATE')")
public class CandidateController {

    private final CandidateService candidateService;
    private final FileStorageService fileStorageService; // Inject Interface vào đây

    // API tải lên file PDF (FR-09)
    @PostMapping("/upload-cv")
    public ResponseEntity<String> uploadCV(@RequestParam("file") MultipartFile file) {
        String fileUrl = fileStorageService.storeFile(file);
        return ResponseEntity.ok(fileUrl);
    }

    // API nộp hồ sơ bằng đường dẫn (FR-07)
    @PostMapping("/jobs/{jobId}/apply")
    public ResponseEntity<String> applyForJob(
            @PathVariable("jobId") Long jobId,
            @RequestBody ApplyJobRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(candidateService.applyForJob(jobId, request, username));
    }
}