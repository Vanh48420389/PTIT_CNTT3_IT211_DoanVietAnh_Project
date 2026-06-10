package org.example.project_java_service.service;

import org.example.project_java_service.model.dto.request.ApplyJobRequest;

public interface CandidateService {
    String applyForJob(Long jobId, ApplyJobRequest request, String username);
}