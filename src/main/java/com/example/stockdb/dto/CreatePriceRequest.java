package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request to add a daily price record")
public record CreatePriceRequest(
        @Schema(description = "Symbol database ID", example = "1")
        @NotNull Long symbolId,

        @Schema(description = "Trading date (ISO format)", example = "2024-01-15")
        @NotBlank String date,

        @Schema(description = "Opening price", example = "150.00")
        @NotNull Double open,

        @Schema(description = "Daily high price", example = "155.00")
        @NotNull Double high,

        @Schema(description = "Daily low price", example = "148.00")
        @NotNull Double low,

        @Schema(description = "Closing price", example = "153.00")
        @NotNull Double close,

        @Schema(description = "Trading volume", example = "50000000")
        @Positive Long volume,

        @Schema(description = "Adjusted closing price (defaults to close if not provided)", example = "153.00")
        Double adjustedClose
) {}
