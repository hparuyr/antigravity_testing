package com.example.stockdb.intraday.alpaca;

import com.example.stockdb.intraday.model.IntradayBar;
import com.example.stockdb.model.Symbol;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.WebSocket;
import java.time.Instant;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class AlpacaWebSocketHandler implements WebSocket.Listener {

    private static final Logger log = LoggerFactory.getLogger(AlpacaWebSocketHandler.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final StringBuilder buffer = new StringBuilder();
    private final String apiKey;
    private final String apiSecret;
    private final Consumer<IntradayBar> onBar;
    private final BiConsumer<Boolean, String> onConnectionEvent;
    private final Consumer<Throwable> onError;
    private WebSocket webSocket;
    private boolean authenticated;

    AlpacaWebSocketHandler(String apiKey, String apiSecret,
                           Consumer<IntradayBar> onBar,
                           BiConsumer<Boolean, String> onConnectionEvent,
                           Consumer<Throwable> onError) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.onBar = onBar;
        this.onConnectionEvent = onConnectionEvent;
        this.onError = onError;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        this.webSocket = webSocket;
        authenticated = false;
        sendAuth();
        onConnectionEvent.accept(true, "connected");
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        buffer.append(data);
        if (last) {
            String full = buffer.toString();
            buffer.setLength(0);
            handleMessage(full);
        }
        webSocket.request(1);
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        log.error("Alpaca WebSocket error: {}", error.getMessage());
        onError.accept(error);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        log.warn("Alpaca WebSocket closed: status={} reason={}", statusCode, reason);
        onConnectionEvent.accept(false, reason);
        return null;
    }

    private void handleMessage(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            if (root.isArray()) {
                for (JsonNode msg : root) {
                    String type = msg.path("T").asText();
                    switch (type) {
                        case "success" -> handleSuccess(msg);
                        case "subscription" -> handleSubscription(msg);
                        case "error" -> handleError(msg);
                        case "b" -> handleBar(msg);
                        default -> log.debug("Unhandled Alpaca message type: {}", type);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Alpaca message: {} — {}", e.getMessage(), json);
        }
    }

    private void handleSuccess(JsonNode msg) {
        String text = msg.path("msg").asText();
        if ("authenticated".equals(text)) {
            authenticated = true;
            log.info("Alpaca WebSocket authenticated");
        }
    }

    private void handleSubscription(JsonNode msg) {
        JsonNode bars = msg.path("bars");
        log.info("Alpaca subscribed to {} symbol(s): {}", bars.size(), bars);
    }

    private void handleError(JsonNode msg) {
        log.error("Alpaca WebSocket error message: {}", msg);
    }

    private void handleBar(JsonNode msg) {
        try {
            IntradayBar bar = new IntradayBar();
            bar.setTimestamp(Instant.parse(msg.path("t").asText()));
            bar.setOpen(msg.path("o").asDouble());
            bar.setHigh(msg.path("h").asDouble());
            bar.setLow(msg.path("l").asDouble());
            bar.setClose(msg.path("c").asDouble());
            bar.setVolume(msg.path("v").asLong());
            bar.setTradeCount(msg.path("n").asInt());
            bar.setVwap(msg.path("vw").asDouble(0));

            Symbol sym = new Symbol();
            sym.setTicker(msg.path("S").asText());
            bar.setSymbol(sym);

            onBar.accept(bar);
        } catch (Exception e) {
            log.error("Failed to parse Alpaca bar: {} — {}", e.getMessage(), msg);
        }
    }

    void sendAuth() {
        if (webSocket != null) {
            String auth = String.format(
                    "{\"action\":\"auth\",\"key\":\"%s\",\"secret\":\"%s\"}", apiKey, apiSecret);
            webSocket.sendText(auth, true);
        }
    }

    void subscribe(String ticker) {
        if (webSocket != null) {
            String sub = String.format(
                    "{\"action\":\"subscribe\",\"bars\":[\"%s\"]}", ticker);
            webSocket.sendText(sub, true);
        }
    }

    void unsubscribe(String ticker) {
        if (webSocket != null) {
            String unsub = String.format(
                    "{\"action\":\"unsubscribe\",\"bars\":[\"%s\"]}", ticker);
            webSocket.sendText(unsub, true);
        }
    }

    void close() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "shutdown");
        }
    }
}
