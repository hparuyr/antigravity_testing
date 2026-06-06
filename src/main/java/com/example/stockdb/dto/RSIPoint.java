package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "RSI value at a point in time")
public record RSIPoint(
        @Schema(description = "Date", example = "2024-01-15") String date,
        @Schema(description = "Relative Strength Index (0-100)", example = "62.50") Double rsi
) {}
