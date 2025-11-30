package com.example.stockdb.service;

import com.example.stockdb.model.DailyPrice;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.stockdb.model.IntradayPrice;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class AlphaVantageService implements StockDataFetcher {

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
    public List<DailyPrice> fetchDailyPrices(String symbol) {
        String url = String.format("%s?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s", apiUrl, symbol, apiKey);

        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                return new ArrayList<>();
            }
            JsonNode root = objectMapper.readTree(response);

            JsonNode timeSeries = root.path("Time Series (Daily)");
            if (timeSeries.isMissingNode()) {
                System.err.println("Error fetching data for " + symbol + ": " + root.toString());
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
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<IntradayPrice> fetchIntradayPrices(String symbol, String interval) {
        String url = String.format("%s?function=TIME_SERIES_INTRADAY&symbol=%s&interval=%s&apikey=%s", apiUrl, symbol,
                interval, apiKey);

        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                return new ArrayList<>();
            }
            JsonNode root = objectMapper.readTree(response);

            JsonNode timeSeries = root.path("Time Series (" + interval + ")");
            if (timeSeries.isMissingNode()) {
                System.err.println("Error fetching intraday data for " + symbol + ": " + root.toString());
                return new ArrayList<>();
            }

            List<IntradayPrice> prices = new ArrayList<>();
            Iterator<Map.Entry<String, JsonNode>> fields = timeSeries.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String dateStr = field.getKey();
                JsonNode data = field.getValue();

                IntradayPrice price = new IntradayPrice();
                price.setTimestamp(dateStr);
                price.setOpen(data.path("1. open").asDouble());
                price.setHigh(data.path("2. high").asDouble());
                price.setLow(data.path("3. low").asDouble());
                price.setClose(data.path("4. close").asDouble());
                price.setVolume(data.path("5. volume").asLong());

                prices.add(price);
            }

            return prices;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
