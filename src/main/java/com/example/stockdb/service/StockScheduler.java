package com.example.stockdb.service;

import com.example.stockdb.model.Exchange;
import com.example.stockdb.model.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import jakarta.annotation.PostConstruct;

@Component
public class StockScheduler {

    private static final Logger logger = LoggerFactory.getLogger(StockScheduler.class);
    private final StockService stockService;

    private final List<String> TICKERS = Arrays.asList("AAPL", "IBM", "GOOGL", "MSFT", "META", "NFLX");

    public StockScheduler(StockService stockService) {
        this.stockService = stockService;
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing StockScheduler...");

        // Ensure default exchange exists
        List<Exchange> exchanges = stockService.getAllExchanges();
        Exchange exchange;
        if (exchanges.isEmpty()) {
            exchange = new Exchange();
            exchange.setName("NASDAQ");
            exchange.setCurrency("USD");
            exchange.setMic("XNAS");
            exchange.setTimezone("America/New_York");
            exchange = stockService.createExchange(exchange);
            logger.info("Created default exchange: NASDAQ");
        } else {
            exchange = exchanges.get(0);
        }

        // Ensure symbols exist
        for (String ticker : TICKERS) {
            try {
                try {
                    stockService.calculateSimpleMovingAverage(ticker, 1); // Just to check existence
                } catch (RuntimeException e) {
                    if (e.getMessage().contains("Symbol not found")) {
                        Symbol symbol = new Symbol();
                        symbol.setTicker(ticker);
                        symbol.setName(ticker + " Inc."); // Placeholder name
                        symbol.setType("Common Stock");
                        symbol.setExchange(exchange);
                        stockService.createSymbol(symbol);
                        logger.info("Created symbol: {}", ticker);
                    } else {
                        throw e;
                    }
                }
            } catch (Exception e) {
                logger.error("Error initializing symbol {}", ticker, e);
            }
        }
    }

    // Run every 24 hours
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    public void fetchDailyData() {
        logger.info("Starting scheduled daily stock data fetch...");
        for (String ticker : TICKERS) {
            try {
                int count = stockService.fetchAndStoreDailyPrices(ticker);
                if (count > 0) {
                    logger.info("Fetched {} daily records for {}", count, ticker);
                } else {
                    logger.warn("No daily records fetched for {}", ticker);
                }
                // Rate limit: 15 seconds delay
                Thread.sleep(15000);
            } catch (Exception e) {
                logger.error("Error fetching daily data for {}", ticker, e);
            }
        }
        logger.info("Completed scheduled daily stock data fetch.");
    }

}
