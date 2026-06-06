package com.example.stockdb.intraday.service;

import com.example.stockdb.exception.SymbolNotFoundException;
import com.example.stockdb.intraday.IntradayDataFetcher;
import com.example.stockdb.intraday.model.IntradayBar;
import com.example.stockdb.intraday.repository.IntradayBarRepository;
import com.example.stockdb.model.Symbol;
import com.example.stockdb.repository.SymbolRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

public class IntradayService {

    private static final Logger log = LoggerFactory.getLogger(IntradayService.class);

    private final IntradayDataFetcher fetcher;
    private final IntradayBarRepository barRepo;
    private final SymbolRepository symbolRepo;

    public IntradayService(IntradayDataFetcher fetcher, IntradayBarRepository barRepo, SymbolRepository symbolRepo) {
        this.fetcher = fetcher;
        this.barRepo = barRepo;
        this.symbolRepo = symbolRepo;
    }

    @PostConstruct
    public void init() {
        fetcher.setBarListener(this::onBar);
        fetcher.start();
        List<Symbol> all = symbolRepo.findAll();
        for (Symbol s : all) {
            fetcher.subscribe(s.getTicker());
        }
        log.info("IntradayService started, subscribed to {} symbols", all.size());
    }

    @PreDestroy
    public void shutdown() {
        fetcher.stop();
        log.info("IntradayService stopped");
    }

    @Transactional
    protected void onBar(IntradayBar bar) {
        try {
            Symbol symbol = symbolRepo.findByTicker(bar.getSymbol().getTicker());
            if (symbol == null) {
                log.warn("Unknown ticker '{}' — skipping bar", bar.getSymbol().getTicker());
                return;
            }
            bar.setSymbol(symbol);
            upsertBar(bar);
        } catch (Exception e) {
            log.error("Failed to store intraday bar for {}: {}", bar.getSymbol().getTicker(), e.getMessage());
        }
    }

    @Transactional
    protected void upsertBar(IntradayBar bar) {
        barRepo.findBySymbolIdAndTimestamp(bar.getSymbol().getId(), bar.getTimestamp())
                .ifPresentOrElse(
                        existing -> {
                            existing.setOpen(bar.getOpen());
                            existing.setHigh(bar.getHigh());
                            existing.setLow(bar.getLow());
                            existing.setClose(bar.getClose());
                            existing.setVolume(bar.getVolume());
                            existing.setTradeCount(bar.getTradeCount());
                            existing.setVwap(bar.getVwap());
                            barRepo.save(existing);
                        },
                        () -> barRepo.save(bar)
                );
    }

    @Transactional(readOnly = true)
    public List<IntradayBar> getBarsSince(String ticker, Instant since) {
        Symbol symbol = symbolRepo.findByTicker(ticker);
        if (symbol == null) {
            throw new SymbolNotFoundException(ticker);
        }
        return barRepo.findBySymbolIdAndTimestampAfterOrderByTimestampAsc(symbol.getId(), since);
    }

    @Transactional(readOnly = true)
    public IntradayBar getLatestBar(String ticker) {
        Symbol symbol = symbolRepo.findByTicker(ticker);
        if (symbol == null) {
            throw new SymbolNotFoundException(ticker);
        }
        return barRepo.findTopBySymbolIdOrderByTimestampDesc(symbol.getId()).orElse(null);
    }

    public boolean isConnected() {
        return fetcher.isConnected();
    }

    public void subscribe(String ticker) {
        fetcher.subscribe(ticker);
    }

    public void unsubscribe(String ticker) {
        fetcher.unsubscribe(ticker);
    }
}
