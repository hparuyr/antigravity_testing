package com.example.stockdb.service;

import com.example.stockdb.model.DailyPrice;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@Profile("!demo")
@ConditionalOnProperty(name = "stock.data.fetcher", havingValue = "alphavantage")
public class AlphaVantageService implements StockDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(AlphaVantageService.class);
    private static final String TIME_SERIES_KEY = "Time Series (Daily)";

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
        String url = String.format(
            "%s?function=TIME_SERIES_DAILY&symbol=%s&outputsize=%s&apikey=%s",
            apiUrl, symbol, outputSize, apiKey
        );

        try {
            JsonNode root = restTemplate.getForObject(url, JsonNode.class);
            if (root == null) {
                return List.of();
            }

            JsonNode timeSeries = root.path(TIME_SERIES_KEY);
            if (timeSeries.isMissingNode()) {
                log.error("Alpha Vantage error for {}: {}", symbol, root);
                return List.of();
            }

            List<DailyPrice> prices = new ArrayList<>();
            Iterator<Map.Entry<String, JsonNode>> fields = timeSeries.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode data = field.getValue();
                prices.add(toDailyPrice(field.getKey(), data));
            }
            return prices;
        } catch (HttpClientErrorException e) {
            HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
            log.error("Alpha Vantage HTTP {} for {}: {} (url: {}?function=TIME_SERIES_DAILY&symbol={}&outputsize={}&apikey=***)",
                status.value(), symbol, e.getResponseBodyAsString(), apiUrl, symbol, outputSize);
            return List.of();
        } catch (ResourceAccessException e) {
            log.error("Alpha Vantage connection error for {}: {} (url: {})", symbol, e.getMessage(), apiUrl);
            return List.of();
        } catch (Exception e) {
            log.error("Alpha Vantage unexpected error for {}: {}", symbol, e.getMessage(), e);
            return List.of();
        }
    }

    private DailyPrice toDailyPrice(String date, JsonNode data) {
        DailyPrice price = new DailyPrice();
        price.setDate(date);
        price.setOpen(data.path("1. open").asDouble());
        price.setHigh(data.path("2. high").asDouble());
        price.setLow(data.path("3. low").asDouble());
        price.setClose(data.path("4. close").asDouble());
        price.setVolume(data.path("5. volume").asLong());
        price.setAdjustedClose(price.getClose());
        return price;
    }
}
