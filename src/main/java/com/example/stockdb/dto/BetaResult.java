package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Beta analysis result against a market benchmark")
public record BetaResult(
        @Schema(description = "Stock ticker", example = "AAPL") String ticker,
        @Schema(description = "Market benchmark ticker", example = "SPY") String marketTicker,
        @Schema(description = "Beta coefficient", example = "1.21") Double beta,
        @Schema(description = "Alpha (intercept)", example = "0.0002") Double alpha,
        @Schema(description = "R-squared", example = "0.65") Double rSquared,
        @Schema(description = "Number of aligned return observations", example = "120") int observations
) {}
