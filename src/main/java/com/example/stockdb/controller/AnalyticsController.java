package com.example.stockdb.controller;

import com.example.stockdb.dto.*;
import com.example.stockdb.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Technical analysis indicators and stock similarity engine")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{ticker}/returns")
    @Operation(summary = "Daily returns", description = "Computes daily simple and log returns for a ticker since a given date")
    public ResponseEntity<List<ReturnPoint>> dailyReturns(
            @Parameter(description = "Stock ticker symbol", example = "AAPL") @PathVariable String ticker,
            @Parameter(description = "Start date (ISO format)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate since) {
        return ResponseEntity.ok(analyticsService.getDailyReturns(ticker, since));
    }

    @GetMapping("/{ticker}/sma")
    @Operation(summary = "Simple Moving Average", description = "Computes the Simple Moving Average over a given period")
    public ResponseEntity<List<MovingAveragePoint>> sma(
            @Parameter(description = "Stock ticker symbol", example = "AAPL") @PathVariable String ticker,
            @Parameter(description = "SMA period in days", example = "20") @RequestParam(defaultValue = "20") int period,
            @Parameter(description = "Start date (ISO format)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate since) {
        return ResponseEntity.ok(analyticsService.getSMA(ticker, period, since));
    }

    @GetMapping("/{ticker}/ema")
    @Operation(summary = "Exponential Moving Average", description = "Computes the Exponential Moving Average over a given period")
    public ResponseEntity<List<MovingAveragePoint>> ema(
            @Parameter(description = "Stock ticker symbol", example = "AAPL") @PathVariable String ticker,
            @Parameter(description = "EMA period in days", example = "20") @RequestParam(defaultValue = "20") int period,
            @Parameter(description = "Start date (ISO format)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate since) {
        return ResponseEntity.ok(analyticsService.getEMA(ticker, period, since));
    }

    @GetMapping("/{ticker}/volatility")
    @Operation(summary = "Rolling volatility", description = "Computes rolling annualized volatility of daily log returns over a given period window")
    public ResponseEntity<List<VolatilityPoint>> volatility(
            @Parameter(description = "Stock ticker symbol", example = "AAPL") @PathVariable String ticker,
            @Parameter(description = "Rolling window in days", example = "21") @RequestParam(defaultValue = "21") int period,
            @Parameter(description = "Start date (ISO format)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate since) {
        return ResponseEntity.ok(analyticsService.getVolatility(ticker, period, since));
    }

    @GetMapping("/{ticker}/rsi")
    @Operation(summary = "Relative Strength Index", description = "Computes the Relative Strength Index over a given period")
    public ResponseEntity<List<RSIPoint>> rsi(
            @Parameter(description = "Stock ticker symbol", example = "AAPL") @PathVariable String ticker,
            @Parameter(description = "RSI period in days", example = "14") @RequestParam(defaultValue = "14") int period,
            @Parameter(description = "Start date (ISO format)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate since) {
        return ResponseEntity.ok(analyticsService.getRSI(ticker, period, since));
    }

    @GetMapping("/{ticker}/macd")
    @Operation(summary = "MACD", description = "Computes MACD line, signal line, and histogram (12, 26, 9)")
    public ResponseEntity<List<MACDPoint>> macd(
            @Parameter(description = "Stock ticker symbol", example = "AAPL") @PathVariable String ticker,
            @Parameter(description = "Start date (ISO format)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate since) {
        return ResponseEntity.ok(analyticsService.getMACD(ticker, since));
    }

    @GetMapping("/{ticker}/bollinger")
    @Operation(summary = "Bollinger Bands", description = "Computes Bollinger Bands (upper, middle, lower) over a given period and standard deviation multiplier")
    public ResponseEntity<List<BollingerPoint>> bollinger(
            @Parameter(description = "Stock ticker symbol", example = "AAPL") @PathVariable String ticker,
            @Parameter(description = "Moving average period in days", example = "20") @RequestParam(defaultValue = "20") int period,
            @Parameter(description = "Standard deviation multiplier", example = "2.0") @RequestParam(defaultValue = "2.0") double multiplier,
            @Parameter(description = "Start date (ISO format)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate since) {
        return ResponseEntity.ok(analyticsService.getBollingerBands(ticker, period, multiplier, since));
    }

    @GetMapping("/{ticker}/vwap")
    @Operation(summary = "VWAP", description = "Computes cumulative Volume-Weighted Average Price")
    public ResponseEntity<List<VWAPPoint>> vwap(
            @Parameter(description = "Stock ticker symbol", example = "AAPL") @PathVariable String ticker,
            @Parameter(description = "Start date (ISO format)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate since) {
        return ResponseEntity.ok(analyticsService.getVWAP(ticker, since));
    }

    @GetMapping("/correlation")
    @Operation(summary = "Pairwise correlation", description = "Computes Pearson correlation of daily log returns between two tickers")
    public ResponseEntity<CorrelationResult> correlation(
            @Parameter(description = "First stock ticker", example = "AAPL") @RequestParam String ticker1,
            @Parameter(description = "Second stock ticker", example = "MSFT") @RequestParam String ticker2,
            @Parameter(description = "Start date (ISO format)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate since) {
        return ResponseEntity.ok(analyticsService.getCorrelation(ticker1, ticker2, since));
    }

    @GetMapping("/{ticker}/beta")
    @Operation(summary = "Beta vs market", description = "Computes Beta, Alpha, and R-squared of a ticker against a market benchmark")
    public ResponseEntity<BetaResult> beta(
            @Parameter(description = "Stock ticker symbol", example = "AAPL") @PathVariable String ticker,
            @Parameter(description = "Market benchmark ticker", example = "SPY") @RequestParam String market,
            @Parameter(description = "Start date (ISO format)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate since) {
        return ResponseEntity.ok(analyticsService.getBeta(ticker, market, since));
    }

    @GetMapping("/{ticker}/mimics")
    @Operation(summary = "Find similar stocks", description = "Compares a ticker against all S&P 500 stocks and returns the top N most correlated. " +
            "Computes Pearson correlation, beta, R-squared, and volatility for every pairwise comparison server-side. " +
            "Single API call replaces 503 individual fetches.")
    public ResponseEntity<MimicsResult> mimics(
            @Parameter(description = "Target stock ticker to find mimics for", example = "AAPL") @PathVariable String ticker,
            @Parameter(description = "Start date for return alignment (ISO format)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate since,
            @Parameter(description = "Maximum number of results to return", example = "20") @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(analyticsService.findMimics(ticker, since, limit));
    }
}
