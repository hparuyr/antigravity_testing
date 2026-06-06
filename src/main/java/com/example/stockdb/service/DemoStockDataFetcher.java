package com.example.stockdb.service;

import com.example.stockdb.model.DailyPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Profile("demo")
public class DemoStockDataFetcher implements StockDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(DemoStockDataFetcher.class);
    private static final int FULL_DAYS = 365;
    private static final int COMPACT_DAYS = 100;
    private static final double DEFAULT_BASE_PRICE = 150.0;
    private static final double VOLATILITY = 0.015;
    private static final long BASE_VOLUME = 1_000_000L;
    private static final long VOLUME_RANGE = 5_000_000L;

    @Override
    public List<DailyPrice> fetchDailyPrices(String symbol, String outputSize) {
        return fetchDailyPrices(symbol, outputSize, null);
    }

    @Override
    public List<DailyPrice> fetchDailyPrices(String symbol, String outputSize, String sinceDate) {
        Random rng = new Random(symbol.hashCode());
        List<DailyPrice> prices = new ArrayList<>();

        if (sinceDate != null) {
            LocalDate start = LocalDate.parse(sinceDate).plusDays(1);
            LocalDate today = LocalDate.now();
            int days = (int) start.until(today).getDays();
            if (days <= 0) return List.of();

            double price = DEFAULT_BASE_PRICE + rng.nextDouble() * 300;
            for (int i = 0; i < days; i++) {
                LocalDate date = start.plusDays(i);
                double change = price * rng.nextGaussian() * VOLATILITY;
                price = Math.max(price + change, 1.0);
                prices.add(buildPrice(date, price, change, rng));
            }
        } else {
            int days = OUTPUT_SIZE_FULL.equals(outputSize) ? FULL_DAYS : COMPACT_DAYS;
            double price = DEFAULT_BASE_PRICE + rng.nextDouble() * 300;

            for (int i = days; i > 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                double change = price * rng.nextGaussian() * VOLATILITY;
                price = Math.max(price + change, 1.0);
                prices.add(buildPrice(date, price, change, rng));
            }
        }

        if (!prices.isEmpty()) {
            log.info("Generated {} demo daily prices for {} (since: {})", prices.size(), symbol, sinceDate != null ? sinceDate : "beginning");
        }
        return prices;
    }

    private DailyPrice buildPrice(LocalDate date, double price, double change, Random rng) {
        DailyPrice dp = new DailyPrice();
        dp.setDate(date.toString());
        dp.setOpen(round(price));
        dp.setHigh(round(price + Math.abs(change) * rng.nextDouble()));
        dp.setLow(round(price - Math.abs(change) * rng.nextDouble()));
        dp.setClose(round(price + change * 0.5));
        dp.setVolume(BASE_VOLUME + rng.nextLong(VOLUME_RANGE));
        dp.setAdjustedClose(dp.getClose());
        return dp;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
