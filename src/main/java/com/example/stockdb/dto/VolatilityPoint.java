package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Rolling volatility value")
public record VolatilityPoint(
        @Schema(description = "Date", example = "2024-01-22") String date,
        @Schema(description = "Standard deviation of log returns over the window", example = "0.015") Double volatility
) {}
