package com.example.stockdb.service;

import com.example.stockdb.model.Exchange;
import com.example.stockdb.model.Symbol;
import com.example.stockdb.repository.SymbolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import jakarta.annotation.PostConstruct;

@Component
public class StockScheduler {

    private static final Logger log = LoggerFactory.getLogger(StockScheduler.class);
    private static final List<String> TICKERS = List.of("AAPL", "IBM", "GOOGL", "MSFT", "META", "NFLX");

    private final StockService stockService;
    private final SymbolRepository symbolRepository;

    public StockScheduler(StockService stockService, SymbolRepository symbolRepository) {
        this.stockService = stockService;
        this.symbolRepository = symbolRepository;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing StockScheduler...");
        List<Exchange> exchanges = stockService.getAllExchanges();
        Exchange exchange;
        if (exchanges.isEmpty()) {
            exchange = new Exchange();
            exchange.setName("NASDAQ");
            exchange.setCurrency("USD");
            exchange.setMic("XNAS");
            exchange.setTimezone("America/New_York");
            exchange = stockService.createExchange(exchange);
            log.info("Created default exchange: NASDAQ");
        } else {
            exchange = exchanges.get(0);
        }
        for (String ticker : TICKERS) {
            if (symbolRepository.findByTicker(ticker) == null) {
                Symbol symbol = new Symbol();
                symbol.setTicker(ticker);
                symbol.setName(ticker + " Inc.");
                symbol.setType("Common Stock");
                symbol.setExchange(exchange);
                stockService.createSymbol(symbol);
                log.info("Created symbol: {}", ticker);
            }
        }
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    public void fetchDailyData() {
        log.info("Starting scheduled daily stock data fetch...");
        for (String ticker : TICKERS) {
            try {
                int count = stockService.fetchAndStoreDailyPrices(ticker);
                if (count > 0) {
                    log.info("Fetched {} daily records for {}", count, ticker);
                } else {
                    log.warn("No daily records fetched for {}", ticker);
                }
                Thread.sleep(15000);
            } catch (Exception e) {
                log.error("Error fetching daily data for {}", ticker, e);
            }
        }
        log.info("Completed scheduled daily stock data fetch.");
    }
}
