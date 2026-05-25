package com.example.stockdb.service;

import com.example.stockdb.model.DailyPrice;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class AlphaVantageService implements StockDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(AlphaVantageService.class);

    @Value("${stock.api.url}")
    private String apiUrl;

    @Value("${stock.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public AlphaVantageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<DailyPrice> fetchDailyPrices(String symbol, String outputSize) {
        String url = String.format("%s?function=TIME_SERIES_DAILY&symbol=%s&outputsize=%s&apikey=%s",
                apiUrl, symbol, outputSize, apiKey);

        try {
            JsonNode root = restTemplate.getForObject(url, JsonNode.class);
            if (root == null) return List.of();

            JsonNode timeSeries = root.path("Time Series (Daily)");
            if (timeSeries.isMissingNode()) {
                log.error("Error fetching data for {}: {}", symbol, root);
                return List.of();
            }

            List<DailyPrice> prices = new ArrayList<>();
            Iterator<Map.Entry<String, JsonNode>> fields = timeSeries.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode data = field.getValue();
                DailyPrice price = new DailyPrice();
                price.setDate(field.getKey());
                price.setOpen(data.path("1. open").asDouble());
                price.setHigh(data.path("2. high").asDouble());
                price.setLow(data.path("3. low").asDouble());
                price.setClose(data.path("4. close").asDouble());
                price.setVolume(data.path("5. volume").asLong());
                price.setAdjustedClose(price.getClose());
                prices.add(price);
            }
            return prices;
        } catch (Exception e) {
            log.error("Error fetching daily prices for {}", symbol, e);
            return List.of();
        }
    }
}
