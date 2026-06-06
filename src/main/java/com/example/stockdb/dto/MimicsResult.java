package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Result of finding stocks that mimic a target ticker")
public record MimicsResult(
        @Schema(description = "Target ticker that was compared", example = "AAPL") String target,
        @Schema(description = "Start date used for return alignment", example = "2024-01-01") String since,
        @Schema(description = "Total number of symbols compared against", example = "502") int totalCompared,
        @Schema(description = "Top matching stocks sorted by correlation descending") List<MimicEntry> matches
) {}
