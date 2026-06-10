package org.example.project_java_service.service.impl;

import org.example.project_java_service.model.dto.request.JobPostingRequest;
import org.example.project_java_service.model.dto.response.ApplicationResponse;
import org.example.project_java_service.model.dto.response.JobPostingResponse;
import org.example.project_java_service.model.entity.JobApplication;
import org.example.project_java_service.model.entity.JobPosting;
import org.example.project_java_service.model.entity.User;
import org.example.project_java_service.model.entity.enumeration.ApplicationStatusEnum;
import org.example.project_java_service.model.entity.enumeration.JobStatusEnum;
import org.example.project_java_service.repository.JobApplicationRepository;
import org.example.project_java_service.repository.JobPostingRepository;
import org.example.project_java_service.repository.UserRepository;
import org.example.project_java_service.service.EmployerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployerServiceImpl implements EmployerService {

    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository; // Thêm repository xử lý hồ sơ

    private User getEmployerByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));
    }

    @Override
    @Transactional
    public String createJob(JobPostingRequest request, String username) {
        User employer = getEmployerByUsername(username);

        JobPosting newJob = JobPosting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .salaryRange(request.getSalaryRange())
                .status(JobStatusEnum.PENDING_APPROVAL) // Vừa đăng xong thì nằm ở trạng thái chờ duyệt
                .employer(employer)
                .build();

        jobPostingRepository.save(newJob);
        return "Đăng tin tuyển dụng thành công. Tin của bạn đang chờ Admin phê duyệt!";
    }

    @Override
    public Page<JobPostingResponse> getMyJobs(String username, int page, int size) {
        User employer = getEmployerByUsername(username);
        Pageable pageable = PageRequest.of(page, size);

        return jobPostingRepository.findByEmployerId(employer.getId(), pageable)
                .map(job -> JobPostingResponse.builder()
                        .id(job.getId())
                        .title(job.getTitle())
                        .description(job.getDescription())
                        .salaryRange(job.getSalaryRange())
                        .status(job.getStatus())
                        .employerUsername(employer.getUsername())
                        .build());
    }

    @Override
    @Transactional
    public String updateJob(Long jobId, JobPostingRequest request, String username) {
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng!"));

        if (!job.getEmployer().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa tin tuyển dụng này!");
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setSalaryRange(request.getSalaryRange());
        job.setStatus(JobStatusEnum.PENDING_APPROVAL); // Sửa bài xong thì Admin phải duyệt lại

        jobPostingRepository.save(job);
        return "Cập nhật thành công. Tin đang được đưa về trạng thái chờ duyệt lại!";
    }

    @Override
    @Transactional
    public String deleteJob(Long jobId, String username) {
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng!"));

        if (!job.getEmployer().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền xóa tin tuyển dụng này!");
        }

        jobPostingRepository.delete(job);
        return "Đã xóa tin tuyển dụng thành công!";
    }

    @Override
    public List<ApplicationResponse> getApplicationsForJob(Long jobId, String username) {
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng!"));

        if (!job.getEmployer().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền xem hồ sơ của tin tuyển dụng này!");
        }

        return jobApplicationRepository.findByJobPostingId(jobId).stream()
                .map(app -> ApplicationResponse.builder()
                        .id(app.getId())
                        .candidateUsername(app.getCandidate().getUsername())
                        .candidateEmail(app.getCandidate().getEmail())
                        .cvUrl(app.getCvUrl())
                        .coverLetter(app.getCoverLetter())
                        .status(app.getStatus())
                        .appliedAt(app.getAppliedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String updateApplicationStatus(Long applicationId, ApplicationStatusEnum status, String username) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ ứng tuyển!"));

        if (!application.getJobPosting().getEmployer().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền thay đổi trạng thái hồ sơ này!");
        }

        application.setStatus(status);
        jobApplicationRepository.save(application);

        return "Đã cập nhật trạng thái hồ sơ thành: " + status.name();
    }
}