package org.example.project_java_service.service;

import org.example.project_java_service.model.dto.response.JobPostingResponse;
import org.example.project_java_service.model.dto.response.UserResponse;
import org.example.project_java_service.model.entity.enumeration.JobStatusEnum;
import org.springframework.data.domain.Page;

public interface AdminService {

    Page<UserResponse> searchUsers(String keyword, int page, int size, String sortBy, String sortDir);

    String toggleUserStatus(Long userId);

    String reviewJob(Long jobId, JobStatusEnum status);

    Page<JobPostingResponse> searchJobs(String keyword, JobStatusEnum status, int page, int size, String sortBy, String sortDir);
}