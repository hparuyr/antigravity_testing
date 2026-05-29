package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current price snapshot")
public record PriceSnapshot(
        @Schema(description = "Latest closing price", example = "195.50") double price,
        @Schema(description = "Price change from previous day", example = "2.30") double change,
        @Schema(description = "Percentage change from previous day", example = "0.0119") double changePercent,
        @Schema(description = "52-week high", example = "198.00") double high52w,
        @Schema(description = "52-week low", example = "165.00") double low52w,
        @Schema(description = "Average daily volume", example = "45200000") long avgVolume
) {}
