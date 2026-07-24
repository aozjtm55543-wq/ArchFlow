package com.archflow.dto.response;

import java.util.List;

public record ProjectBlueprintResponse(
    ProjectSummaryDto projectSummary,
    ReadmeDto readme,
    List<ApiSpecDto> apiSpecifications,
    List<TableDto> databaseSchema,
    List<DirectoryDto> directoryStructure,
    List<ChecklistDto> developmentChecklist
) {
    public record ProjectSummaryDto(
        String title,
        String description,
        String architectureReasoning
    ) {}

    public record ReadmeDto(
        String overview,
        List<String> features,
        List<String> gettingStarted
    ) {}

    public record ApiSpecDto(
        String domain,
        String method,
        String endpoint,
        String summary,
        boolean authRequired,
        Object requestPayload,
        Object responsePayload
    ) {}

    public record TableDto(
        String tableName,
        String description,
        List<ColumnDto> columns
    ) {}

    public record ColumnDto(
        String name,
        String dataType,
        boolean isPrimaryKey,
        boolean isNullable,
        String description
    ) {}

    public record DirectoryDto(
        String path,
        String role
    ) {}

    public record ChecklistDto(
        String phase,
        List<String> tasks
    ) {}
}
