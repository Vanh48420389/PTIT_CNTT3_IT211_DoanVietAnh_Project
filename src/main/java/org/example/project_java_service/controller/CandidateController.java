package org.example.project_java_service.controller;

import org.example.project_java_service.model.dto.request.ApplyJobRequest;
import org.example.project_java_service.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/candidate")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_CANDIDATE')")
public class CandidateController {

    private final CandidateService candidateService;

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