package com.example.stockdb.service;

import com.example.stockdb.model.DailyPrice;

import java.util.List;

public interface StockDataFetcher {

    String OUTPUT_SIZE_FULL = "full";
    String OUTPUT_SIZE_COMPACT = "compact";

    List<DailyPrice> fetchDailyPrices(String symbol, String outputSize);
}
