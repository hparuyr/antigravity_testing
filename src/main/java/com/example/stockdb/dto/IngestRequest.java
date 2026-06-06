package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;

import java.util.List;

@Schema(description = "Request to ingest prices for one or more tickers")
public record IngestRequest(
        @Schema(description = "List of ticker symbols to ingest", example = "[\"AAPL\",\"MSFT\",\"GOOGL\"]")
        List<String> tickers,

        @Schema(description = "Set to true to ingest all S&P 500 symbols", example = "true")
        boolean all
) {
    public IngestRequest {
        if (tickers == null) tickers = List.of();
    }

    @AssertTrue(message = "Provide tickers or set all=true")
    public boolean isValid() {
        return all || !tickers.isEmpty();
    }
}
