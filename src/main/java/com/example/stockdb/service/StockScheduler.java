package com.example.stockdb.service;

import com.example.stockdb.model.Symbol;
import com.example.stockdb.repository.SymbolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!demo")
public class StockScheduler {

    private static final Logger log = LoggerFactory.getLogger(StockScheduler.class);
    private static final long TICKER_DELAY_MS = 15_000;

    private final StockService stockService;
    private final SymbolRepository symbolRepository;

    public StockScheduler(StockService stockService, SymbolRepository symbolRepository) {
        this.stockService = stockService;
        this.symbolRepository = symbolRepository;
    }

    @Scheduled(cron = "0 0 6 * * *")
    public void fetchDailyData() {
        List<Symbol> symbols = symbolRepository.findAll();
        log.info("Starting scheduled daily fetch for {} symbols...", symbols.size());

        for (Symbol symbol : symbols) {
            try {
                int count = stockService.fetchAndStoreDailyPrices(symbol.getTicker());
                if (count > 0) {
                    log.info("Fetched {} daily records for {}", count, symbol.getTicker());
                } else {
                    log.warn("No daily records fetched for {}", symbol.getTicker());
                }
                Thread.sleep(TICKER_DELAY_MS);
            } catch (Exception e) {
                log.error("Error fetching daily data for {}", symbol.getTicker(), e);
            }
        }
        log.info("Completed scheduled daily stock data fetch.");
    }
}
