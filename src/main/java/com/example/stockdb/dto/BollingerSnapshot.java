package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Bollinger Bands values at a point in time")
public record BollingerSnapshot(
        @Schema(description = "Upper band (middle + k * std)", example = "198.50") double upper,
        @Schema(description = "Middle band (SMA)", example = "190.20") double middle,
        @Schema(description = "Lower band (middle - k * std)", example = "181.90") double lower
) {}
