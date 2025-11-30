package com.example.stockdb.service;

import com.example.stockdb.model.DailyPrice;
import com.example.stockdb.model.Exchange;
import com.example.stockdb.model.Symbol;
import com.example.stockdb.repository.DailyPriceRepository;
import com.example.stockdb.repository.ExchangeRepository;
import com.example.stockdb.repository.SymbolRepository;
import com.example.stockdb.model.IntradayPrice;
import com.example.stockdb.repository.IntradayPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StockService {

    private final ExchangeRepository exchangeRepository;
    private final SymbolRepository symbolRepository;
    private final DailyPriceRepository dailyPriceRepository;
    private final IntradayPriceRepository intradayPriceRepository;
    private final StockDataFetcher stockDataFetcher;

    @Autowired
    public StockService(ExchangeRepository exchangeRepository, SymbolRepository symbolRepository,
            DailyPriceRepository dailyPriceRepository, IntradayPriceRepository intradayPriceRepository,
            StockDataFetcher stockDataFetcher) {
        this.exchangeRepository = exchangeRepository;
        this.symbolRepository = symbolRepository;
        this.dailyPriceRepository = dailyPriceRepository;
        this.intradayPriceRepository = intradayPriceRepository;
        this.stockDataFetcher = stockDataFetcher;
    }

    // Exchange Operations
    public List<Exchange> getAllExchanges() {
        return exchangeRepository.findAll();
    }

    public Exchange createExchange(Exchange exchange) {
        return exchangeRepository.save(exchange);
    }

    // Symbol Operations
    public List<Symbol> getAllSymbols() {
        return symbolRepository.findAll();
    }

    public List<Symbol> getSymbolsByExchange(Long exchangeId) {
        return symbolRepository.findByExchangeId(exchangeId);
    }

    public Symbol createSymbol(Symbol symbol) {
        return symbolRepository.save(symbol);
    }

    // Daily Price Operations
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

        List<DailyPrice> prices = stockDataFetcher.fetchDailyPrices(ticker);
        int count = 0;
        for (DailyPrice price : prices) {
            price.setSymbol(symbol);
            DailyPrice existingPrice = dailyPriceRepository.findBySymbolIdAndDate(symbol.getId(), price.getDate());
            if (existingPrice != null) {
                existingPrice.setOpen(price.getOpen());
                existingPrice.setHigh(price.getHigh());
                existingPrice.setLow(price.getLow());
                existingPrice.setClose(price.getClose());
                existingPrice.setVolume(price.getVolume());
                existingPrice.setAdjustedClose(price.getAdjustedClose());
                dailyPriceRepository.save(existingPrice);
            } else {
                dailyPriceRepository.save(price);
            }
            count++;
        }
        return count;
    }

    public Double calculateSimpleMovingAverage(String ticker, int days) {
        Symbol symbol = symbolRepository.findByTicker(ticker);
        if (symbol == null) {
            throw new RuntimeException("Symbol not found: " + ticker);
        }

        List<DailyPrice> prices = dailyPriceRepository.findBySymbolId(symbol.getId());
        // Sort by date descending (assuming repository doesn't sort)
        prices.sort((p1, p2) -> p2.getDate().compareTo(p1.getDate()));

        if (prices.size() < days) {
            return null; // Not enough data
        }

        double sum = 0;
        for (int i = 0; i < days; i++) {
            sum += prices.get(i).getClose();
        }

        return sum / days;
    }

    @Transactional
    public int fetchAndStoreIntradayPrices(String ticker, String interval) {
        Symbol symbol = symbolRepository.findByTicker(ticker);
        if (symbol == null) {
            throw new RuntimeException("Symbol not found: " + ticker);
        }

        List<IntradayPrice> prices = stockDataFetcher.fetchIntradayPrices(ticker, interval);
        int count = 0;
        for (IntradayPrice price : prices) {
            price.setSymbol(symbol);
            IntradayPrice existingPrice = intradayPriceRepository.findBySymbolIdAndTimestamp(symbol.getId(),
                    price.getTimestamp());
            if (existingPrice != null) {
                existingPrice.setOpen(price.getOpen());
                existingPrice.setHigh(price.getHigh());
                existingPrice.setLow(price.getLow());
                existingPrice.setClose(price.getClose());
                existingPrice.setVolume(price.getVolume());
                intradayPriceRepository.save(existingPrice);
            } else {
                intradayPriceRepository.save(price);
            }
            count++;
        }
        return count;
    }

    public List<IntradayPrice> getIntradayPricesSince(String ticker, LocalDateTime since) {
        Symbol symbol = symbolRepository.findByTicker(ticker);
        if (symbol == null) {
            throw new RuntimeException("Symbol not found: " + ticker);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return intradayPriceRepository.findBySymbolIdAndTimestampGreaterThanEqual(symbol.getId(),
                since.format(formatter));
    }

    public List<DailyPrice> getDailyPricesSince(String ticker, LocalDate since) {
        Symbol symbol = symbolRepository.findByTicker(ticker);
        if (symbol == null) {
            throw new RuntimeException("Symbol not found: " + ticker);
        }
        // Assuming DailyPriceRepository has a similar method or we filter in memory (or
        // add to repo)
        // Let's add to repo first? Or just use findAll and filter?
        // DailyPrice date is String "yyyy-MM-dd".
        // Let's check DailyPriceRepository.
        // It only has findBySymbolId. Let's filter in memory for now or add to repo.
        // Adding to repo is better. But let's check if I can modify repo in this step.
        // I'll filter in memory for DailyPrice to avoid another file edit if possible,
        // or edit repo next.
        // Actually, let's edit DailyPriceRepository too.
        // Wait, I can't edit DailyPriceRepository in this multi_replace call for
        // StockService.
        // I'll filter in memory for now, it's safer for this step.
        List<DailyPrice> allPrices = dailyPriceRepository.findBySymbolId(symbol.getId());
        String sinceStr = since.toString();
        return allPrices.stream()
                .filter(p -> p.getDate().compareTo(sinceStr) >= 0)
                .sorted((p1, p2) -> p2.getDate().compareTo(p1.getDate())) // Newest first
                .toList();
    }
}
