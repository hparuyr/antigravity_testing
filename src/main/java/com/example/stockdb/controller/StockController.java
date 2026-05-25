package com.example.stockdb.controller;

import com.example.stockdb.model.DailyPrice;
import com.example.stockdb.model.Exchange;
import com.example.stockdb.model.Symbol;
import com.example.stockdb.service.StockService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/exchanges")
    public List<Exchange> getAllExchanges() {
        return stockService.getAllExchanges();
    }

    @PostMapping("/exchanges")
    public Exchange createExchange(@RequestBody Exchange exchange) {
        return stockService.createExchange(exchange);
    }

    @GetMapping("/symbols")
    public List<Symbol> getAllSymbols() {
        return stockService.getAllSymbols();
    }

    @GetMapping("/exchanges/{exchangeId}/symbols")
    public List<Symbol> getSymbolsByExchange(@PathVariable Long exchangeId) {
        return stockService.getSymbolsByExchange(exchangeId);
    }

    @PostMapping("/symbols")
    public Symbol createSymbol(@RequestBody Symbol symbol) {
        return stockService.createSymbol(symbol);
    }

    @GetMapping("/symbols/{symbolId}/prices")
    public List<DailyPrice> getPricesBySymbol(@PathVariable Long symbolId) {
        return stockService.getPricesBySymbol(symbolId);
    }

    @PostMapping("/prices")
    public DailyPrice addPrice(@RequestBody DailyPrice price) {
        return stockService.addPrice(price);
    }

    @GetMapping("/daily/{ticker}")
    public List<DailyPrice> getDailyPrices(@PathVariable String ticker, @RequestParam String since) {
        return stockService.getDailyPricesSince(ticker, LocalDate.parse(since));
    }
}
