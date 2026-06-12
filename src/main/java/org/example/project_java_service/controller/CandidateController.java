package org.example.project_java_service.controller;

import org.example.project_java_service.model.dto.request.ApplyJobRequest;
import org.example.project_java_service.service.CandidateService;
import org.example.project_java_service.service.FileStorageService;
import jakarta.validation.Valid;
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
    private final FileStorageService fileStorageService;

    @PostMapping("/upload-cv")
    public ResponseEntity<String> uploadCV(@RequestParam("file") MultipartFile file) {
        String fileUrl = fileStorageService.storeFile(file);
        return ResponseEntity.ok(fileUrl);
    }

    @PostMapping("/jobs/{jobId}/apply")
    public ResponseEntity<String> applyForJob(
            @PathVariable("jobId") Long jobId,
            @Valid @RequestBody ApplyJobRequest request, // Thêm @Valid vào đây
            Authentication authentication
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(candidateService.applyForJob(jobId, request, username));
    }
}