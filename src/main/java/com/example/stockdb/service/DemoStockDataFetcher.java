package com.example.stockdb.service;

import com.example.stockdb.model.DailyPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@Profile("demo")
public class DemoStockDataFetcher implements StockDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(DemoStockDataFetcher.class);

    private static final Map<String, Double> BASE_PRICES = new LinkedHashMap<>();
    static {
        BASE_PRICES.put("AAPL", 175.0);
        BASE_PRICES.put("IBM",  190.0);
        BASE_PRICES.put("GOOGL", 140.0);
        BASE_PRICES.put("MSFT", 380.0);
        BASE_PRICES.put("META", 320.0);
        BASE_PRICES.put("NFLX", 480.0);
    }

    @Override
    public List<DailyPrice> fetchDailyPrices(String symbol, String outputSize) {
        Double base = BASE_PRICES.get(symbol);
        if (base == null) {
            log.warn("No demo base price for {}", symbol);
            return List.of();
        }

        Random rng = new Random(symbol.hashCode());
        int days = "full".equals(outputSize) ? 365 : 100;
        List<DailyPrice> prices = new ArrayList<>(days);
        double price = base;

        for (int i = days; i > 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            double change = price * (rng.nextGaussian() * 0.015);
            price = Math.max(price + change, 1.0);

            DailyPrice dp = new DailyPrice();
            dp.setDate(date.toString());
            dp.setOpen(Math.round(price * 100.0) / 100.0);
            dp.setHigh(Math.round((price + Math.abs(change) * rng.nextDouble()) * 100.0) / 100.0);
            dp.setLow(Math.round((price - Math.abs(change) * rng.nextDouble()) * 100.0) / 100.0);
            dp.setClose(Math.round((price + change * 0.5) * 100.0) / 100.0);
            dp.setVolume((long) (1_000_000 + rng.nextLong(5_000_000)));
            dp.setAdjustedClose(dp.getClose());
            prices.add(dp);
        }

        log.info("Generated {} demo daily prices for {}", prices.size(), symbol);
        return prices;
    }
}
