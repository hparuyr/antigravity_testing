package com.example.stockdb.service;

import com.example.stockdb.model.Exchange;
import com.example.stockdb.model.Symbol;
import com.example.stockdb.repository.ExchangeRepository;
import com.example.stockdb.repository.SymbolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.util.List;

@Component
@Profile("demo")
public class DemoDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final List<String> TICKERS = List.of("AAPL", "IBM", "GOOGL", "MSFT", "META", "NFLX");

    private final StockService stockService;
    private final ExchangeRepository exchangeRepository;
    private final SymbolRepository symbolRepository;

    public DemoDataSeeder(StockService stockService, ExchangeRepository exchangeRepository, SymbolRepository symbolRepository) {
        this.stockService = stockService;
        this.exchangeRepository = exchangeRepository;
        this.symbolRepository = symbolRepository;
    }

    @PostConstruct
    public void seed() {
        log.info("Seeding demo data...");
        List<Exchange> exchanges = exchangeRepository.findAll();
        Exchange exchange;
        if (exchanges.isEmpty()) {
            exchange = new Exchange();
            exchange.setName("NASDAQ");
            exchange.setCurrency("USD");
            exchange.setMic("XNAS");
            exchange.setTimezone("America/New_York");
            exchange = exchangeRepository.save(exchange);
            log.info("Created default exchange: NASDAQ");
        } else {
            exchange = exchanges.get(0);
        }
        for (String ticker : TICKERS) {
            Symbol symbol = symbolRepository.findByTicker(ticker);
            if (symbol == null) {
                symbol = new Symbol();
                symbol.setTicker(ticker);
                symbol.setName(ticker + " Inc.");
                symbol.setType("Common Stock");
                symbol.setExchange(exchange);
                symbolRepository.save(symbol);
                log.info("Created symbol: {}", ticker);
            }
            stockService.fetchAndStoreDailyPrices(ticker);
        }
        log.info("Demo data seeding complete.");
    }
}
