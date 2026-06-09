package com.example.stockdb.service;

import com.example.stockdb.model.Symbol;
import com.example.stockdb.repository.SymbolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StockScheduler {

    private static final Logger log = LoggerFactory.getLogger(StockScheduler.class);

    static final int INITIAL_BACKFILL_LIMIT = 25;

    @Value("${stock.api.delay}")
    private long tickerDelayMs;

    private final StockService stockService;
    private final SymbolRepository symbolRepository;

    public StockScheduler(StockService stockService, SymbolRepository symbolRepository) {
        this.stockService = stockService;
        this.symbolRepository = symbolRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialFetch() {
        log.info("Scheduling initial backfill ({} tickers) on startup...", INITIAL_BACKFILL_LIMIT);
        new Thread(() -> {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            fetchAllSymbols("Initial backfill", INITIAL_BACKFILL_LIMIT);
        }).start();
    }

    @Scheduled(cron = "0 0 6 * * *")
    public void fetchDailyData() {
        fetchAllSymbols("Scheduled daily fetch");
    }

    private void fetchAllSymbols(String label) {
        fetchAllSymbols(label, Integer.MAX_VALUE);
    }

    private void fetchAllSymbols(String label, int limit) {
        List<Symbol> symbols = symbolRepository.findAll();
        if (symbols.size() > limit) {
            symbols = symbols.stream().limit(limit).collect(Collectors.toList());
        }
        log.info("{}: starting for {}/{} symbols...", label, symbols.size(), symbolRepository.count());

        for (Symbol symbol : symbols) {
            try {
                int count = stockService.fetchAndStoreDailyPrices(symbol.getTicker());
                if (count > 0) {
                    log.info("{}: fetched {} daily records for {}", label, count, symbol.getTicker());
                } else {
                    log.warn("{}: no daily records fetched for {}", label, symbol.getTicker());
                }
                Thread.sleep(tickerDelayMs);
            } catch (Exception e) {
                log.error("{}: error fetching daily data for {}", label, symbol.getTicker(), e);
            }
        }
        log.info("{}: complete.", label);
    }
}
