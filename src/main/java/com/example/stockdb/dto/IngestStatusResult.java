package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current ingestion status for a ticker (no API call)")
public record IngestStatusResult(
        @Schema(description = "Stock ticker symbol", example = "AAPL") String ticker,
        @Schema(description = "Whether the symbol exists in the database", example = "true") boolean symbolExists,
        @Schema(description = "Total stored price records", example = "120") int totalRecords,
        @Schema(description = "Earliest date with data", example = "2024-01-02") String dateFrom,
        @Schema(description = "Latest date with data", example = "2024-06-15") String dateTo
) {}
