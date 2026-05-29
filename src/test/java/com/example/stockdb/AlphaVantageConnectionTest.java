package com.example.stockdb;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the Alpha Vantage daily price endpoint
 * (the default data fetcher after Finnhub free tier was removed).
 */
class AlphaVantageConnectionTest {

    @Test
    void alphavantageDailyPricesForAapl() {
        String apiKey = System.getenv("STOCK_API_KEY");
        assertNotNull(apiKey, "STOCK_API_KEY env var must be set");

        String apiUrl = System.getenv("STOCK_API_URL") != null
            ? System.getenv("STOCK_API_URL")
            : "https://www.alphavantage.co/query";

        RestTemplate restTemplate = new RestTemplate();
        String url = String.format(
            "%s?function=TIME_SERIES_DAILY&symbol=AAPL&outputsize=compact&apikey=%s",
            apiUrl, apiKey
        );

        JsonNode root = restTemplate.getForObject(url, JsonNode.class);

        assertNotNull(root);
        JsonNode timeSeries = root.path("Time Series (Daily)");
        assertFalse(timeSeries.isMissingNode(),
            "Response must contain 'Time Series (Daily)'. If you get a 403/5xx, your STOCK_API_KEY may be a " +
            "Finnhub key — get a free Alpha Vantage key at https://www.alphavantage.co/support/#api-key");

        assertTrue(timeSeries.size() > 0,
            "Time Series (Daily) must contain at least one day of data");

        JsonNode latest = timeSeries.fields().next().getValue();
        assertTrue(latest.has("1. open"));
        assertTrue(latest.has("4. close"));
        assertTrue(latest.path("4. close").asDouble() > 0,
            "AAPL close price should be positive");
    }
}
