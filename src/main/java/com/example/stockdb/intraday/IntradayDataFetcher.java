package com.example.stockdb.intraday;

import com.example.stockdb.intraday.model.IntradayBar;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

public interface IntradayDataFetcher {

    void start();

    void stop();

    void subscribe(String ticker);

    void unsubscribe(String ticker);

    List<IntradayBar> fetchHistorical(String ticker, Instant since);

    boolean isConnected();

    void setBarListener(Consumer<IntradayBar> listener);
}
