package org.example.project_java_service.service.impl;

import org.example.project_java_service.model.dto.request.ApplyJobRequest;
import org.example.project_java_service.model.entity.JobApplication;
import org.example.project_java_service.model.entity.JobPosting;
import org.example.project_java_service.model.entity.User;
import org.example.project_java_service.model.entity.enumeration.JobStatusEnum;
import org.example.project_java_service.repository.JobApplicationRepository;
import org.example.project_java_service.repository.JobPostingRepository;
import org.example.project_java_service.repository.UserRepository;
import org.example.project_java_service.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public String applyForJob(Long jobId, ApplyJobRequest request, String username) {
        User candidate = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản ứng viên!"));

        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công việc này!"));

        if (job.getStatus() != JobStatusEnum.APPROVED) {
            throw new RuntimeException("Công việc này hiện không nhận hồ sơ!");
        }

        if (jobApplicationRepository.existsByJobPostingIdAndCandidateId(jobId, candidate.getId())) {
            throw new RuntimeException("Bạn đã nộp hồ sơ cho công việc này rồi!");
        }

        JobApplication application = JobApplication.builder()
                .jobPosting(job)
                .candidate(candidate)
                .cvUrl(request.getCvUrl())
                .coverLetter(request.getCoverLetter())
                .build();

        jobApplicationRepository.save(application);
        return "Nộp hồ sơ thành công! Chúc bạn may mắn.";
    }
}