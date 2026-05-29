package com.example.stockdb.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Technical indicators snapshot at the latest available date")
public record IndicatorSnapshot(
        @Schema(description = "20-day Simple Moving Average", example = "190.20") Double sma20,
        @Schema(description = "50-day Simple Moving Average", example = "185.10") Double sma50,
        @Schema(description = "12-day Exponential Moving Average", example = "192.30") Double ema12,
        @Schema(description = "26-day Exponential Moving Average", example = "188.70") Double ema26,
        @Schema(description = "14-day Relative Strength Index", example = "62.50") Double rsi14,
        @Schema(description = "MACD (12, 26, 9)") MacdSnapshot macd,
        @Schema(description = "Bollinger Bands (20, 2)") BollingerSnapshot bollinger,
        @Schema(description = "21-day rolling volatility (std of log returns)", example = "0.015") Double volatility21d,
        @Schema(description = "Beta vs SPY over the last year", example = "1.21") Double beta1y,
        @Schema(description = "Correlation with SPY over the last year", example = "0.89") Double correlationSpy
) {}
