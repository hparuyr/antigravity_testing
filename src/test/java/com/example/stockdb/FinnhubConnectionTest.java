package com.example.stockdb;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Finnhub free-tier endpoints.
 * Finnhub free plan includes {@code /quote} (current OHLC) and {@code /stock/profile2}
 * but does NOT include {@code /stock/candle} (requires paid plan).
 */
class FinnhubConnectionTest {

    @Test
    void finnhubQuoteWithXFinnhubToken() {
        String apiKey = System.getenv("STOCK_API_KEY");
        assertNotNull(apiKey, "STOCK_API_KEY env var must be set");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Finnhub-Token", apiKey);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        String url = "https://finnhub.io/api/v1/quote?symbol=AAPL";

        ResponseEntity<JsonNode> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, JsonNode.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().has("c"),
            "quote response must contain current price field 'c'");
        assertTrue(response.getBody().get("c").asDouble() > 0,
            "AAPL current price should be positive");
    }

    @Test
    void finnhubQuoteWithTokenUrlParam() {
        String apiKey = System.getenv("STOCK_API_KEY");
        assertNotNull(apiKey, "STOCK_API_KEY env var must be set");

        RestTemplate restTemplate = new RestTemplate();
        String url = String.format(
            "https://finnhub.io/api/v1/quote?symbol=AAPL&token=%s", apiKey
        );

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().has("c"));
    }
}
