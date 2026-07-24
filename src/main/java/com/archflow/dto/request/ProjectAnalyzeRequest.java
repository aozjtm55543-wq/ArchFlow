package com.archflow.dto.request;

import com.archflow.dto.response.ProjectBlueprintResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ProjectAnalyzeRequest(@NotNull @Valid ProjectBlueprintResponse projectBlueprint) {}
