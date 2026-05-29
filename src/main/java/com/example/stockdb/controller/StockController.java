package com.example.stockdb.controller;

import com.example.stockdb.dto.*;
import com.example.stockdb.model.DailyPrice;
import com.example.stockdb.model.Exchange;
import com.example.stockdb.model.Symbol;
import com.example.stockdb.service.ContextService;
import com.example.stockdb.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Stock Data", description = "Core stock data endpoints: exchanges, symbols, prices, ingestion, and context")
public class StockController {

    private final StockService stockService;
    private final ContextService contextService;

    public StockController(StockService stockService, ContextService contextService) {
        this.stockService = stockService;
        this.contextService = contextService;
    }

    @GetMapping("/exchanges")
    @Operation(summary = "List all exchanges", description = "Returns all stock exchanges in the database")
    public ResponseEntity<List<Exchange>> getAllExchanges() {
        return ResponseEntity.ok(stockService.getAllExchanges());
    }

    @GetMapping("/exchanges/{id}")
    @Operation(summary = "Get exchange by ID", description = "Returns a single exchange by its database ID")
    public ResponseEntity<Exchange> getExchange(
            @Parameter(description = "Exchange database ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(stockService.getExchangeById(id));
    }

    @PostMapping("/exchanges")
    @Operation(summary = "Create an exchange", description = "Creates a new stock exchange entry")
    @ApiResponse(responseCode = "201", description = "Exchange created")
    public ResponseEntity<Exchange> createExchange(@Valid @RequestBody CreateExchangeRequest request) {
        Exchange created = stockService.createExchange(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/symbols")
    @Operation(summary = "List all symbols", description = "Returns all stock symbols in the database (all S&P 500)")
    public ResponseEntity<List<Symbol>> getAllSymbols() {
        return ResponseEntity.ok(stockService.getAllSymbols());
    }

    @GetMapping("/exchanges/{exchangeId}/symbols")
    @Operation(summary = "List symbols by exchange", description = "Returns all symbols belonging to a specific exchange")
    public ResponseEntity<List<Symbol>> getSymbolsByExchange(
            @Parameter(description = "Exchange database ID", example = "1") @PathVariable Long exchangeId) {
        return ResponseEntity.ok(stockService.getSymbolsByExchange(exchangeId));
    }

    @PostMapping("/symbols")
    @Operation(summary = "Create a symbol", description = "Creates a new stock symbol under an existing exchange")
    @ApiResponse(responseCode = "201", description = "Symbol created")
    public ResponseEntity<Symbol> createSymbol(@Valid @RequestBody CreateSymbolRequest request) {
        Symbol created = stockService.createSymbol(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/symbols/{symbolId}/prices")
    @Operation(summary = "Get prices by symbol ID", description = "Returns all daily price records for a given symbol database ID")
    public ResponseEntity<List<DailyPrice>> getPricesBySymbol(
            @Parameter(description = "Symbol database ID", example = "1") @PathVariable Long symbolId) {
        return ResponseEntity.ok(stockService.getPricesBySymbol(symbolId));
    }

    @PostMapping("/prices")
    @Operation(summary = "Add a price record", description = "Manually inserts a single daily price record")
    @ApiResponse(responseCode = "201", description = "Price record created")
    public ResponseEntity<DailyPrice> addPrice(@Valid @RequestBody CreatePriceRequest request) {
        DailyPrice price = new DailyPrice();
        price.setSymbol(stockService.getSymbolById(request.symbolId()));
        price.setDate(request.date());
        price.setOpen(request.open());
        price.setHigh(request.high());
        price.setLow(request.low());
        price.setClose(request.close());
        price.setVolume(request.volume());
        price.setAdjustedClose(request.adjustedClose() != null ? request.adjustedClose() : request.close());
        DailyPrice created = stockService.addPrice(price);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/daily/{ticker}")
    @Operation(summary = "Get daily prices for a ticker", description = "Returns daily OHLCV prices for a ticker since a given date")
    public ResponseEntity<List<DailyPrice>> getDailyPrices(
            @Parameter(description = "Stock ticker symbol", example = "AAPL") @PathVariable String ticker,
            @Parameter(description = "Start date (ISO format)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate since) {
        return ResponseEntity.ok(stockService.getDailyPricesSince(ticker, since));
    }

    @PostMapping("/ingest/{ticker}")
    @Operation(summary = "Ingest prices for a ticker", description = "Fetches daily prices from the external data source for a single ticker. Returns metadata about what was fetched.")
    public ResponseEntity<IngestResult> ingestTicker(
            @Parameter(description = "Stock ticker symbol", example = "AAPL") @PathVariable String ticker) {
        return ResponseEntity.ok(stockService.ingestTicker(ticker));
    }

    @PostMapping("/ingest")
    @Operation(summary = "Batch ingest prices", description = "Fetches daily prices for multiple tickers or all S&P 500 at once. " +
            "Provide a list of tickers or set 'all: true' to ingest every symbol.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
            @ExampleObject(name = "Specific tickers", value = "{\"tickers\":[\"AAPL\",\"MSFT\",\"GOOGL\"]}"),
            @ExampleObject(name = "All S&P 500", value = "{\"all\":true}")
    }))
    public ResponseEntity<List<IngestResult>> ingestBatch(@Valid @RequestBody IngestRequest request) {
        return ResponseEntity.ok(stockService.ingestBatch(request));
    }

    @GetMapping("/ingest/{ticker}/status")
    @Operation(summary = "Check ingestion status", description = "Returns metadata about stored prices for a ticker without calling the external API")
    public ResponseEntity<IngestStatusResult> ingestStatus(
            @Parameter(description = "Stock ticker symbol", example = "AAPL") @PathVariable String ticker) {
        return ResponseEntity.ok(stockService.getIngestStatus(ticker));
    }

    @GetMapping("/context/{ticker}")
    @Operation(summary = "Get complete stock context", description = "Returns a comprehensive stock profile in one call: metadata, price snapshot, 10 technical indicators (SMA20/50, EMA12/26, RSI14, MACD, Bollinger, volatility, beta vs SPY, correlation vs SPY), and full price history. Designed for AI consumption.")
    public ResponseEntity<StockContext> stockContext(
            @Parameter(description = "Stock ticker symbol", example = "AAPL") @PathVariable String ticker,
            @Parameter(description = "Start date for price history and indicator computation (ISO format)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate since) {
        return ResponseEntity.ok(contextService.getStockContext(ticker, since));
    }
}
