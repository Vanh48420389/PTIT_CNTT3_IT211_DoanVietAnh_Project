package org.example.project_java_service.service.impl;

import org.example.project_java_service.model.dto.response.JobPostingResponse;
import org.example.project_java_service.model.dto.response.UserResponse;
import org.example.project_java_service.model.entity.JobPosting;
import org.example.project_java_service.model.entity.User;
import org.example.project_java_service.model.entity.enumeration.JobStatusEnum;
import org.example.project_java_service.model.entity.enumeration.RoleEnum;
import org.example.project_java_service.repository.JobPostingRepository;
import org.example.project_java_service.repository.UserRepository;
import org.example.project_java_service.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final JobPostingRepository jobPostingRepository;

    @Override
    public Page<UserResponse> searchUsers(String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            userPage = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return userPage.map(user -> UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.isActive())
                .build());
    }

    @Override
    @Transactional
    public String toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        if (user.getRole() == RoleEnum.ROLE_ADMIN) {
            throw new RuntimeException("Không thể khóa tài khoản có quyền Quản trị (Admin)!");
        }
        user.setActive(!user.isActive());
        userRepository.save(user);

        return user.isActive() ? "Đã mở khóa tài khoản thành công!" : "Đã khóa tài khoản thành công!";
    }

    @Override
    public Page<JobPostingResponse> searchJobs(String keyword, JobStatusEnum status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<JobPosting> jobPage = jobPostingRepository.searchJobs(keyword != null ? keyword : "", status, pageable);

        return jobPage.map(job -> JobPostingResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .salaryRange(job.getSalaryRange())
                .status(job.getStatus())
                .employerUsername(job.getEmployer().getUsername())
                .build());
    }

    @Override
    @Transactional
    public String reviewJob(Long jobId, JobStatusEnum status) {
        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng với ID: " + jobId));

        if (jobPosting.getStatus() != JobStatusEnum.PENDING_APPROVAL) {
            throw new RuntimeException("Chỉ có thể duyệt các tin đang chờ duyệt (PENDING_APPROVAL)!");
        }

        jobPosting.setStatus(status);
        jobPostingRepository.save(jobPosting);

        return "Đã cập nhật trạng thái tin tuyển dụng thành: " + status.name();
    }
}