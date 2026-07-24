package com.archflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ProjectGenerateRequest(
    @NotBlank String projectName,
    @NotBlank String description,
    @NotBlank String techStack,
    List<String> requiredFeatures
) {}
