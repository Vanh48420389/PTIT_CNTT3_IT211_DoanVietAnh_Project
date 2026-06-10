package org.example.project_java_service.repository;

import org.example.project_java_service.model.entity.JobPosting;
import org.example.project_java_service.model.entity.enumeration.JobStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    @Query("SELECT j FROM JobPosting j WHERE " +
            "(:status IS NULL OR j.status = :status) AND " +
            "LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<JobPosting> searchJobs(String keyword, JobStatusEnum status, Pageable pageable);
    Page<JobPosting> findByEmployerId(Long employerId, Pageable pageable);
}