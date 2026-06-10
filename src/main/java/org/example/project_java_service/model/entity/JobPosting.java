package org.example.project_java_service.model.entity;

import org.example.project_java_service.model.entity.enumeration.JobStatusEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_postings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private String salaryRange;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private JobStatusEnum status = JobStatusEnum.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer;
}