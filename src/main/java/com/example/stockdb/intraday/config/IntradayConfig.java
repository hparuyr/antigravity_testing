package com.example.stockdb.intraday.config;

import com.example.stockdb.intraday.IntradayDataFetcher;
import com.example.stockdb.intraday.alpaca.AlpacaIntradayFetcher;
import com.example.stockdb.intraday.service.IntradayService;
import com.example.stockdb.intraday.repository.IntradayBarRepository;
import com.example.stockdb.repository.SymbolRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnProperty(name = "intraday.enabled", havingValue = "true")
public class IntradayConfig {

    @Bean
    @ConfigurationProperties(prefix = "alpaca")
    IntradayProperties alpacaProperties() {
        return new IntradayProperties();
    }

    @Bean
    IntradayDataFetcher intradayDataFetcher(IntradayProperties props, RestTemplate restTemplate) {
        String provider = props.getProvider() != null ? props.getProvider() : "alpaca";
        if ("alpaca".equals(provider)) {
            return new AlpacaIntradayFetcher(props, restTemplate);
        }
        throw new IllegalArgumentException("Unknown intraday provider: " + provider);
    }

    @Bean
    IntradayService intradayService(
            IntradayDataFetcher fetcher,
            IntradayBarRepository barRepo,
            SymbolRepository symbolRepo) {
        return new IntradayService(fetcher, barRepo, symbolRepo);
    }
}
