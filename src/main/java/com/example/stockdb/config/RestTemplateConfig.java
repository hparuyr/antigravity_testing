package com.example.stockdb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);

        RestTemplate restTemplate = new RestTemplate(factory);

        restTemplate.setInterceptors(List.of(userAgentInterceptor()));

        return restTemplate;
    }

    private ClientHttpRequestInterceptor userAgentInterceptor() {
        return (request, body, execution) -> {
            request.getHeaders().set("User-Agent", "StockDashboard/1.0");
            request.getHeaders().set("Accept", "application/json");
            return execution.execute(request, body);
        };
    }
}
