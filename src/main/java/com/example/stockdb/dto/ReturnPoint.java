package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Daily return value")
public record ReturnPoint(
        @Schema(description = "Date", example = "2024-01-02") String date,
        @Schema(description = "Simple return (close_t - close_t-1) / close_t-1", example = "0.012") Double simpleReturn,
        @Schema(description = "Log return ln(close_t / close_t-1)", example = "0.0119") Double logReturn
) {}
