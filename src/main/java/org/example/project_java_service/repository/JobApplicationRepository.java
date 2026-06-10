package org.example.project_java_service.repository;

import org.example.project_java_service.model.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByJobPostingIdAndCandidateId(Long jobPostingId, Long candidateId);
    List<JobApplication> findByJobPostingId(Long jobPostingId);
}