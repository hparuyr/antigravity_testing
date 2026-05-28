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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("!demo")
@ConditionalOnProperty(name = "stock.data.fetcher", havingValue = "finnhub")
public class FinnhubService implements StockDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(FinnhubService.class);
    private static final int COMPACT_DAYS = 100;

    @Value("${stock.api.url}")
    private String apiUrl;

    @Value("${stock.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public FinnhubService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<DailyPrice> fetchDailyPrices(String symbol, String outputSize) {
        long now = Instant.now().getEpochSecond();
        long from = OUTPUT_SIZE_FULL.equals(outputSize)
            ? 0
            : Instant.now().minus(COMPACT_DAYS, ChronoUnit.DAYS).getEpochSecond();

        String url = String.format(
            "%s/stock/candle?symbol=%s&resolution=D&from=%d&to=%d&token=%s",
            apiUrl, symbol, from, now, apiKey
        );

        try {
            JsonNode root = restTemplate.getForObject(url, JsonNode.class);
            if (root == null || !"ok".equals(root.path("s").asText())) {
                log.warn("Finnhub returned error for {}: {}", symbol, root);
                return List.of();
            }

            JsonNode timestamps = root.path("t");
            JsonNode opens = root.path("o");
            JsonNode highs = root.path("h");
            JsonNode lows = root.path("l");
            JsonNode closes = root.path("c");
            JsonNode volumes = root.path("v");

            List<DailyPrice> prices = new ArrayList<>();
            for (int i = 0; i < timestamps.size(); i++) {
                prices.add(toDailyPrice(timestamps, opens, highs, lows, closes, volumes, i));
            }
            return prices;
        } catch (HttpClientErrorException e) {
            HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
            log.error("Finnhub HTTP {} for {}: {} (url: {}?symbol={}&resolution=D&from={}&to={}&token=***)",
                status.value(), symbol, e.getResponseBodyAsString(), apiUrl, symbol, from, now);
            return List.of();
        } catch (ResourceAccessException e) {
            log.error("Finnhub connection error for {}: {} (url: {})", symbol, e.getMessage(), apiUrl);
            return List.of();
        } catch (Exception e) {
            log.error("Finnhub unexpected error for {}: {}", symbol, e.getMessage(), e);
            return List.of();
        }
    }

    private DailyPrice toDailyPrice(JsonNode timestamps, JsonNode opens, JsonNode highs,
                                     JsonNode lows, JsonNode closes, JsonNode volumes, int i) {
        long ts = timestamps.get(i).asLong();
        String date = Instant.ofEpochSecond(ts).atZone(ZoneOffset.UTC).toLocalDate().toString();

        DailyPrice price = new DailyPrice();
        price.setDate(date);
        price.setOpen(opens.get(i).asDouble());
        price.setHigh(highs.get(i).asDouble());
        price.setLow(lows.get(i).asDouble());
        price.setClose(closes.get(i).asDouble());
        price.setVolume(volumes.get(i).asLong());
        price.setAdjustedClose(closes.get(i).asDouble());
        return price;
    }
}
