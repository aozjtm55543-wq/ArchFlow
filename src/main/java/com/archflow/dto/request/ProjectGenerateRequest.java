package com.archflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProjectGenerateRequest(
    @NotBlank @Size(max = 100) String projectName,
    @NotBlank @Size(max = 2_000) String description,
    @NotBlank @Size(max = 500) String techStack,
    @Size(max = 20) List<@Size(max = 100) String> requiredFeatures
) {}
