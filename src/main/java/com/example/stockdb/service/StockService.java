package com.example.stockdb.service;

import com.example.stockdb.model.DailyPrice;
import com.example.stockdb.model.Exchange;
import com.example.stockdb.model.Symbol;
import com.example.stockdb.repository.DailyPriceRepository;
import com.example.stockdb.repository.ExchangeRepository;
import com.example.stockdb.repository.SymbolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class StockService {

    private final ExchangeRepository exchangeRepository;
    private final SymbolRepository symbolRepository;
    private final DailyPriceRepository dailyPriceRepository;
    private final StockDataFetcher stockDataFetcher;

    public StockService(ExchangeRepository exchangeRepository, SymbolRepository symbolRepository,
            DailyPriceRepository dailyPriceRepository, StockDataFetcher stockDataFetcher) {
        this.exchangeRepository = exchangeRepository;
        this.symbolRepository = symbolRepository;
        this.dailyPriceRepository = dailyPriceRepository;
        this.stockDataFetcher = stockDataFetcher;
    }

    public List<Exchange> getAllExchanges() {
        return exchangeRepository.findAll();
    }

    public Exchange createExchange(Exchange exchange) {
        return exchangeRepository.save(exchange);
    }

    public List<Symbol> getAllSymbols() {
        return symbolRepository.findAll();
    }

    public List<Symbol> getSymbolsByExchange(Long exchangeId) {
        return symbolRepository.findByExchangeId(exchangeId);
    }

    public Symbol createSymbol(Symbol symbol) {
        return symbolRepository.save(symbol);
    }

    public List<DailyPrice> getPricesBySymbol(Long symbolId) {
        return dailyPriceRepository.findBySymbolId(symbolId);
    }

    public DailyPrice addPrice(DailyPrice price) {
        return dailyPriceRepository.save(price);
    }

    @Transactional
    public int fetchAndStoreDailyPrices(String ticker) {
        Symbol symbol = symbolRepository.findByTicker(ticker);
        if (symbol == null) {
            throw new RuntimeException("Symbol not found: " + ticker);
        }
        String outputSize = dailyPriceRepository.countBySymbolId(symbol.getId()) == 0 ? "full" : "compact";
        List<DailyPrice> prices = stockDataFetcher.fetchDailyPrices(ticker, outputSize);
        for (DailyPrice price : prices) {
            price.setSymbol(symbol);
            DailyPrice existing = dailyPriceRepository.findBySymbolIdAndDate(symbol.getId(), price.getDate());
            if (existing != null) {
                existing.setOpen(price.getOpen());
                existing.setHigh(price.getHigh());
                existing.setLow(price.getLow());
                existing.setClose(price.getClose());
                existing.setVolume(price.getVolume());
                existing.setAdjustedClose(price.getAdjustedClose());
                dailyPriceRepository.save(existing);
            } else {
                dailyPriceRepository.save(price);
            }
        }
        return prices.size();
    }

    public List<DailyPrice> getDailyPricesSince(String ticker, LocalDate since) {
        Symbol symbol = symbolRepository.findByTicker(ticker);
        if (symbol == null) {
            throw new RuntimeException("Symbol not found: " + ticker);
        }
        String sinceStr = since.toString();
        return dailyPriceRepository.findBySymbolId(symbol.getId()).stream()
                .filter(p -> p.getDate().compareTo(sinceStr) >= 0)
                .sorted(Comparator.comparing(DailyPrice::getDate).reversed())
                .toList();
    }
}
