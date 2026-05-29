package com.example.stockdb.service;

import com.example.stockdb.model.Exchange;
import com.example.stockdb.model.Symbol;
import com.example.stockdb.repository.ExchangeRepository;
import com.example.stockdb.repository.SymbolRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("demo")
public class DemoDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final StockService stockService;
    private final ExchangeRepository exchangeRepository;
    private final SymbolRepository symbolRepository;
    private final Sp500Loader sp500Loader;

    public DemoDataSeeder(StockService stockService, ExchangeRepository exchangeRepository,
                          SymbolRepository symbolRepository, Sp500Loader sp500Loader) {
        this.stockService = stockService;
        this.exchangeRepository = exchangeRepository;
        this.symbolRepository = symbolRepository;
        this.sp500Loader = sp500Loader;
    }

    @PostConstruct
    @Transactional
    public void seed() {
        log.info("Seeding demo data...");
        Map<String, Exchange> exchangeCache = new LinkedHashMap<>();

        for (Sp500Loader.Sp500Entry entry : sp500Loader.loadAll()) {
            Exchange exchange = exchangeCache.computeIfAbsent(
                entry.exchangeMic(), mic -> getOrCreateExchange(mic));

            if (symbolRepository.findByTicker(entry.ticker()) == null) {
                Symbol symbol = new Symbol();
                symbol.setTicker(entry.ticker());
                symbol.setName(entry.name());
                symbol.setType("Common Stock");
                symbol.setExchange(exchange);
                symbolRepository.save(symbol);
            }

            stockService.fetchAndStoreDailyPrices(entry.ticker());
        }
        log.info("Demo data seeding complete.");
    }

    private Exchange getOrCreateExchange(String mic) {
        Exchange exchange = exchangeRepository.findByMic(mic);
        if (exchange == null) {
            String name = "XNAS".equals(mic) ? "NASDAQ" : "NYSE";
            String tz = "America/New_York";
            exchange = new Exchange();
            exchange.setMic(mic);
            exchange.setName(name);
            exchange.setCurrency("USD");
            exchange.setTimezone(tz);
            exchangeRepository.save(exchange);
            log.info("Created exchange: {} ({})", name, mic);
        }
        return exchange;
    }
}
