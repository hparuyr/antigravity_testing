package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Bollinger Bands at a point in time")
public record BollingerPoint(
        @Schema(description = "Date", example = "2024-01-15") String date,
        @Schema(description = "Upper band", example = "198.50") Double upper,
        @Schema(description = "Middle band (SMA)", example = "190.20") Double middle,
        @Schema(description = "Lower band", example = "181.90") Double lower
) {}
