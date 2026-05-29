package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Moving average value at a point in time")
public record MovingAveragePoint(
        @Schema(description = "Date", example = "2024-01-15") String date,
        @Schema(description = "Moving average value", example = "190.20") Double value
) {}
