package org.example.project_java_service.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplyJobRequest {
    @NotBlank(message = "Link CV không được để trống")
    private String cvUrl;

    private String coverLetter;
}