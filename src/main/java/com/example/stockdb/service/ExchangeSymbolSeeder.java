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
@Profile("!demo")
public class ExchangeSymbolSeeder {

    private static final Logger log = LoggerFactory.getLogger(ExchangeSymbolSeeder.class);

    private final ExchangeRepository exchangeRepository;
    private final SymbolRepository symbolRepository;
    private final Sp500Loader sp500Loader;

    public ExchangeSymbolSeeder(ExchangeRepository exchangeRepository,
                                SymbolRepository symbolRepository,
                                Sp500Loader sp500Loader) {
        this.exchangeRepository = exchangeRepository;
        this.symbolRepository = symbolRepository;
        this.sp500Loader = sp500Loader;
    }

    @PostConstruct
    @Transactional
    public void seed() {
        log.info("Seeding exchanges and symbols...");
        seedExchanges();
        seedSymbols();
        log.info("Exchange and symbol seeding complete.");
    }

    private void seedExchanges() {
        Map<String, ExchangeInfo> exchangeDefs = Map.of(
            "XNAS", new ExchangeInfo("NASDAQ", "USD", "America/New_York"),
            "XNYS", new ExchangeInfo("NYSE", "USD", "America/New_York")
        );

        for (var def : exchangeDefs.entrySet()) {
            String mic = def.getKey();
            ExchangeInfo info = def.getValue();
            if (exchangeRepository.findByMic(mic) == null) {
                Exchange exchange = new Exchange();
                exchange.setMic(mic);
                exchange.setName(info.name);
                exchange.setCurrency(info.currency);
                exchange.setTimezone(info.timezone);
                exchangeRepository.save(exchange);
                log.info("Created exchange: {} ({})", info.name, mic);
            }
        }
    }

    private void seedSymbols() {
        List<Sp500Loader.Sp500Entry> entries = sp500Loader.loadAll();
        Map<String, Exchange> exchangeCache = new LinkedHashMap<>();

        for (Sp500Loader.Sp500Entry entry : entries) {
            Exchange exchange = exchangeCache.computeIfAbsent(
                entry.exchangeMic(), mic -> exchangeRepository.findByMic(mic));
            if (exchange == null) {
                log.warn("Exchange {} not found for ticker {}", entry.exchangeMic(), entry.ticker());
                continue;
            }

            if (symbolRepository.findByTicker(entry.ticker()) == null) {
                Symbol symbol = new Symbol();
                symbol.setTicker(entry.ticker());
                symbol.setName(entry.name());
                symbol.setType("Common Stock");
                symbol.setExchange(exchange);
                symbolRepository.save(symbol);
            }
        }
        log.info("Seeded {} S&P 500 symbols", entries.size());
    }

    private record ExchangeInfo(String name, String currency, String timezone) {}
}
