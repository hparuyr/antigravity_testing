package com.example.stockdb.service;

import com.example.stockdb.model.DailyPrice;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final Logger logger = LoggerFactory.getLogger(AlphaVantageService.class);

    @Value("${stock.api.url}")
    private String apiUrl;

    @Value("${stock.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AlphaVantageService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<DailyPrice> fetchDailyPrices(String symbol, String outputSize) {
        String url = String.format("%s?function=TIME_SERIES_DAILY&symbol=%s&outputsize=%s&apikey=%s", apiUrl, symbol,
                outputSize, apiKey);

        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                return new ArrayList<>();
            }
            JsonNode root = objectMapper.readTree(response);

            JsonNode timeSeries = root.path("Time Series (Daily)");
            if (timeSeries.isMissingNode()) {
                logger.error("Error fetching data for {}: {}", symbol, root.toString());
                return new ArrayList<>();
            }

            List<DailyPrice> prices = new ArrayList<>();
            Iterator<Map.Entry<String, JsonNode>> fields = timeSeries.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String dateStr = field.getKey();
                JsonNode data = field.getValue();

                DailyPrice price = new DailyPrice();
                price.setDate(dateStr);
                price.setOpen(data.path("1. open").asDouble());
                price.setHigh(data.path("2. high").asDouble());
                price.setLow(data.path("3. low").asDouble());
                price.setClose(data.path("4. close").asDouble());
                price.setVolume(data.path("5. volume").asLong());
                price.setAdjustedClose(price.getClose()); // Alpha Vantage free tier doesn't give adjusted close in this
                                                          // endpoint

                prices.add(price);
            }

            return prices;

        } catch (Exception e) {
            logger.error("Error fetching daily prices for {}", symbol, e);
            return new ArrayList<>();
        }
    }

}
