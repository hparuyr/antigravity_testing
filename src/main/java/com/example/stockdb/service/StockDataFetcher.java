package com.example.stockdb.service;

import com.example.stockdb.model.DailyPrice;
import com.example.stockdb.model.IntradayPrice;
import java.util.List;

public interface StockDataFetcher {
    List<DailyPrice> fetchDailyPrices(String symbol);

    List<IntradayPrice> fetchIntradayPrices(String symbol, String interval);
}
