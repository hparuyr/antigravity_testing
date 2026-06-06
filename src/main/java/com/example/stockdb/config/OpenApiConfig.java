package com.example.stockdb.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI stockDbOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Stock Dashboard API")
                        .description("REST API for stock data ingestion, technical analysis, and AI-powered stock comparison. " +
                                "Provides daily OHLCV prices, technical indicators (SMA, EMA, RSI, MACD, Bollinger, VWAP, Volatility), " +
                                "correlation analysis, and a mimics endpoint for finding similarly-behaving stocks across the S&P 500.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("StockDB Team")
                                .email("dev@stockdb.example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
