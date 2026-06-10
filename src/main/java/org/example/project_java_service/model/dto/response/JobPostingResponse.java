package org.example.project_java_service.model.dto.response;

import org.example.project_java_service.model.entity.enumeration.JobStatusEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobPostingResponse {
    private Long id;
    private String title;
    private String description;
    private String salaryRange;
    private JobStatusEnum status;
    private String employerUsername;
}