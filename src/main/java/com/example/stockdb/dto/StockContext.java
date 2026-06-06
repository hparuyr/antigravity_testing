package com.example.stockdb.dto;

import com.example.stockdb.model.DailyPrice;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Complete stock context for AI consumption: metadata, snapshot, indicators, and price history")
public record StockContext(
        @Schema(description = "Stock ticker symbol", example = "AAPL") String ticker,
        @Schema(description = "Company name", example = "Apple Inc.") String name,
        @Schema(description = "Exchange MIC", example = "XNAS") String exchange,
        @Schema(description = "Earliest date in the returned price range", example = "2024-01-02") String periodFrom,
        @Schema(description = "Latest date in the returned price range", example = "2024-06-15") String periodTo,
        @Schema(description = "Number of trading days in the range", example = "118") int tradingDays,
        @Schema(description = "Current price snapshot with 52-week range") PriceSnapshot snapshot,
        @Schema(description = "Technical indicators at the latest date") IndicatorSnapshot indicators,
        @Schema(description = "Daily OHLCV price series (most recent first)") List<DailyPrice> prices
) {}
