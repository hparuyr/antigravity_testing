package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "MACD values at a point in time")
public record MACDPoint(
        @Schema(description = "Date", example = "2024-01-15") String date,
        @Schema(description = "MACD line (12 EMA - 26 EMA)", example = "2.10") Double macd,
        @Schema(description = "Signal line (9 EMA of MACD)", example = "1.80") Double signal,
        @Schema(description = "Histogram (MACD - Signal)", example = "0.30") Double histogram
) {}
