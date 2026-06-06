package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to create a new stock symbol")
public record CreateSymbolRequest(
        @Schema(description = "Exchange database ID", example = "1")
        @NotNull Long exchangeId,

        @Schema(description = "Stock ticker symbol", example = "AAPL")
        @NotBlank String ticker,

        @Schema(description = "Company name", example = "Apple Inc.")
        @NotBlank String name,

        @Schema(description = "Security type", example = "Common Stock")
        @NotBlank String type
) {}
