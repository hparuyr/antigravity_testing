package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of a ticker ingestion operation")
public record IngestResult(
        @Schema(description = "Stock ticker symbol", example = "AAPL") String ticker,
        @Schema(description = "Ingestion status: loaded (new data fetched), exists (data already existed), empty (no data)", example = "loaded") String status,
        @Schema(description = "Number of price records fetched from the API", example = "100") int recordsFetched,
        @Schema(description = "Net new records stored (fetched minus duplicates)", example = "95") int recordsStored,
        @Schema(description = "Total price records in database for this ticker", example = "120") int totalRecords,
        @Schema(description = "Earliest date with data", example = "2024-01-02") String dateFrom,
        @Schema(description = "Latest date with data", example = "2024-06-15") String dateTo
) {}
