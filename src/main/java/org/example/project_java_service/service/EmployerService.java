package org.example.project_java_service.service;

import org.example.project_java_service.model.dto.request.JobPostingRequest;
import org.example.project_java_service.model.dto.response.ApplicationResponse;
import org.example.project_java_service.model.dto.response.JobPostingResponse;
import org.example.project_java_service.model.entity.enumeration.ApplicationStatusEnum;
import org.springframework.data.domain.Page;

import java.util.List;

public interface EmployerService {
    // 1. Đăng tin mới
    String createJob(JobPostingRequest request, String username);

    // 2. Lấy danh sách tin của chính mình
    Page<JobPostingResponse> getMyJobs(String username, int page, int size);

    // 3. Sửa tin
    String updateJob(Long jobId, JobPostingRequest request, String username);

    // 4. Xóa tin
    String deleteJob(Long jobId, String username);

    // 5. Xem danh sách ứng viên nộp vào 1 công việc cụ thể
    List<ApplicationResponse> getApplicationsForJob(Long jobId, String username);

    // 6. Cập nhật trạng thái hồ sơ
    String updateApplicationStatus(Long applicationId, ApplicationStatusEnum status, String username);
}