package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "VWAP value at a point in time")
public record VWAPPoint(
        @Schema(description = "Date", example = "2024-01-15") String date,
        @Schema(description = "Cumulative Volume-Weighted Average Price", example = "191.50") Double vwap
) {}
