package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A single matching stock in the mimics comparison")
public record MimicEntry(
        @Schema(description = "Rank by correlation (1 = most similar)", example = "1") int rank,
        @Schema(description = "Stock ticker symbol", example = "MSFT") String ticker,
        @Schema(description = "Company name", example = "Microsoft Corporation") String name,
        @Schema(description = "Exchange MIC", example = "XNAS") String exchange,
        @Schema(description = "Pearson correlation of daily log returns", example = "0.87") double correlation,
        @Schema(description = "Beta vs target ticker", example = "1.15") double beta,
        @Schema(description = "R-squared of returns vs target", example = "0.76") double rSquared,
        @Schema(description = "Daily log return standard deviation", example = "0.018") double volatility
) {}
