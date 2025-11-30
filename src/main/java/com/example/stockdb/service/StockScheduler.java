package com.example.stockdb.service;

import com.example.stockdb.model.Exchange;
import com.example.stockdb.model.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

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
                // Check if symbol exists (this will throw exception if not found in current
                // implementation,
                // but let's check properly or handle exception)
                // The current service throws RuntimeException if not found.
                // Let's rely on repository directly or catch exception?
                // Better to use a safe check if possible, but service throws.
                // Let's try to fetch, if exception, create.
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

    // Run every 12 hours
    @Scheduled(fixedRate = 12 * 60 * 60 * 1000)
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

    // Run every 20 minutes
    @Scheduled(fixedRate = 20 * 60 * 1000)
    public void fetchIntradayData() {
        logger.info("Starting scheduled intraday stock data fetch...");
        for (String ticker : TICKERS) {
            try {
                int intradayCount = stockService.fetchAndStoreIntradayPrices(ticker, "1min");
                if (intradayCount > 0) {
                    logger.info("Fetched {} intraday (1min) records for {}", intradayCount, ticker);
                } else {
                    logger.warn("No intraday records fetched for {}", ticker);
                }
                // Rate limit: 15 seconds delay
                Thread.sleep(15000);
            } catch (Exception e) {
                logger.error("Error fetching intraday data for {}", ticker, e);
            }
        }
        logger.info("Completed scheduled intraday stock data fetch.");
    }
}
