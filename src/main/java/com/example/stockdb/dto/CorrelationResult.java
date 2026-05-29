package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pearson correlation between two tickers")
public record CorrelationResult(
        @Schema(description = "First ticker", example = "AAPL") String ticker1,
        @Schema(description = "Second ticker", example = "MSFT") String ticker2,
        @Schema(description = "Pearson correlation coefficient", example = "0.87") Double correlation,
        @Schema(description = "Number of aligned return observations", example = "118") int observations
) {}
