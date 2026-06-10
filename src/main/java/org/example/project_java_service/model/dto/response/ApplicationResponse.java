package org.example.project_java_service.model.dto.response;

import org.example.project_java_service.model.entity.enumeration.ApplicationStatusEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationResponse {
    private Long id;
    private String candidateUsername;
    private String candidateEmail;
    private String cvUrl;
    private String coverLetter;
    private ApplicationStatusEnum status;
    private LocalDateTime appliedAt;
}