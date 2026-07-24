package com.archflow.dto.response;

import java.util.List;

public record ProjectAnalyzeResponse(
    List<String> consistencyIssues,
    List<String> recommendedMissingFeatures,
    String difficultyLevel,
    String technicalBottleneck
) {}
