package com.example.stockdb.intraday.config;

import java.util.ArrayList;
import java.util.List;

public class IntradayProperties {

    private boolean enabled = false;
    private String provider = "alpaca";
    private String apiKey = "";
    private String apiSecret = "";
    private String wsUrl = "wss://stream.data.alpaca.markets/v2/iex";
    private String restUrl = "https://data.alpaca.markets/v2";
    private List<String> symbols = new ArrayList<>();
    private int maxReconnectDelaySec = 60;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getApiSecret() { return apiSecret; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }

    public String getWsUrl() { return wsUrl; }
    public void setWsUrl(String wsUrl) { this.wsUrl = wsUrl; }

    public String getRestUrl() { return restUrl; }
    public void setRestUrl(String restUrl) { this.restUrl = restUrl; }

    public List<String> getSymbols() { return symbols; }
    public void setSymbols(List<String> symbols) { this.symbols = symbols; }

    public int getMaxReconnectDelaySec() { return maxReconnectDelaySec; }
    public void setMaxReconnectDelaySec(int maxReconnectDelaySec) { this.maxReconnectDelaySec = maxReconnectDelaySec; }
}
