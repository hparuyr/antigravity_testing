package com.example.stockdb.service;

import com.example.stockdb.model.DailyPrice;

import java.util.List;

public interface StockDataFetcher {

    String OUTPUT_SIZE_FULL = "full";
    String OUTPUT_SIZE_COMPACT = "compact";

    default List<DailyPrice> fetchDailyPrices(String symbol, String outputSize) {
        return fetchDailyPrices(symbol, outputSize, null);
    }

    List<DailyPrice> fetchDailyPrices(String symbol, String outputSize, String sinceDate);
}
