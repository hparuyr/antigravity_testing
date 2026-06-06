package com.example.stockdb.intraday.alpaca;

import com.example.stockdb.intraday.IntradayDataFetcher;
import com.example.stockdb.intraday.config.IntradayProperties;
import com.example.stockdb.intraday.model.IntradayBar;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AlpacaIntradayFetcher implements IntradayDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(AlpacaIntradayFetcher.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final IntradayProperties props;
    private final RestTemplate restTemplate;
    private final Set<String> pendingSubscriptions = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private HttpClient httpClient;
    private AlpacaWebSocketHandler wsHandler;
    private WebSocket webSocket;
    private Consumer<IntradayBar> barListener;
    private volatile boolean connected;
    private int reconnectAttempt;

    public AlpacaIntradayFetcher(IntradayProperties props, RestTemplate restTemplate) {
        this.props = props;
        this.restTemplate = restTemplate;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public void start() {
        log.info("Starting Alpaca intraday fetcher — connecting to {}", props.getWsUrl());
        connect();
    }

    @Override
    public void stop() {
        log.info("Stopping Alpaca intraday fetcher");
        scheduler.shutdownNow();
        if (wsHandler != null) {
            wsHandler.close();
        }
        connected = false;
    }

    @Override
    public void subscribe(String ticker) {
        if (connected && webSocket != null) {
            wsHandler.subscribe(ticker);
        } else {
            pendingSubscriptions.add(ticker);
        }
    }

    @Override
    public void unsubscribe(String ticker) {
        if (connected && webSocket != null) {
            wsHandler.unsubscribe(ticker);
        }
        pendingSubscriptions.remove(ticker);
    }

    @Override
    public List<IntradayBar> fetchHistorical(String ticker, Instant since) {
        try {
            String url = String.format(
                    "%s/stocks/%s/bars?timeframe=1Min&start=%s&limit=1000",
                    props.getRestUrl(), ticker, since.toString());

            HttpHeaders headers = new HttpHeaders();
            headers.set("APCA-API-KEY-ID", props.getApiKey());
            headers.set("APCA-API-SECRET-KEY", props.getApiSecret());

            var exchange = restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), JsonNode.class);
            JsonNode body = exchange.getBody();
            if (body == null || !body.has("bars")) {
                return List.of();
            }

            JsonNode barsNode = body.path("bars");
            List<IntradayBar> result = new ArrayList<>();
            for (JsonNode node : barsNode) {
                IntradayBar bar = new IntradayBar();
                bar.setTimestamp(Instant.parse(node.path("t").asText()));
                bar.setOpen(node.path("o").asDouble());
                bar.setHigh(node.path("h").asDouble());
                bar.setLow(node.path("l").asDouble());
                bar.setClose(node.path("c").asDouble());
                bar.setVolume(node.path("v").asLong());
                bar.setTradeCount(node.path("n").asInt());
                bar.setVwap(node.path("vw").asDouble(0));
                result.add(bar);
            }
            return result;
        } catch (Exception e) {
            log.error("Alpaca historical fetch failed for {}: {}", ticker, e.getMessage());
            return List.of();
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void setBarListener(Consumer<IntradayBar> listener) {
        this.barListener = listener;
    }

    private void connect() {
        try {
            wsHandler = new AlpacaWebSocketHandler(
                    props.getApiKey(),
                    props.getApiSecret(),
                    bar -> {
                        if (barListener != null) {
                            barListener.accept(bar);
                        }
                    },
                    (ok, msg) -> {
                        connected = ok;
                        if (ok) {
                            reconnectAttempt = 0;
                            resubscribePending();
                        } else {
                            scheduleReconnect();
                        }
                    },
                    error -> scheduleReconnect()
            );

            webSocket = httpClient.newWebSocketBuilder()
                    .buildAsync(URI.create(props.getWsUrl()), wsHandler)
                    .join();
        } catch (Exception e) {
            log.error("Failed to connect to Alpaca WebSocket: {}", e.getMessage());
            scheduleReconnect();
        }
    }

    private void resubscribePending() {
        for (String ticker : pendingSubscriptions) {
            wsHandler.subscribe(ticker);
        }
    }

    private void scheduleReconnect() {
        if (scheduler.isShutdown()) return;
        int delay = Math.min(1 << reconnectAttempt, props.getMaxReconnectDelaySec());
        reconnectAttempt++;
        log.info("Scheduling Alpaca reconnect in {}s (attempt {})", delay, reconnectAttempt);
        scheduler.schedule(this::connect, delay, TimeUnit.SECONDS);
    }
}
