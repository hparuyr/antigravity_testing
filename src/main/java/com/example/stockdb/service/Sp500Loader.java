package com.example.stockdb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class Sp500Loader {

    private static final Logger log = LoggerFactory.getLogger(Sp500Loader.class);
    private static final String CSV_PATH = "sp500.csv";

    public record Sp500Entry(String ticker, String name, String exchangeMic) {}

    public List<Sp500Entry> loadAll() {
        List<Sp500Entry> entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(CSV_PATH).getInputStream()))) {

            String header = reader.readLine();
            if (header == null) {
                log.warn("sp500.csv is empty");
                return entries;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                entries.add(parseLine(line));
            }

            log.info("Loaded {} S&P 500 entries", entries.size());
        } catch (IOException e) {
            log.error("Failed to load sp500.csv", e);
        }
        return entries;
    }

    private Sp500Entry parseLine(String line) {
        int firstComma = line.indexOf(',');
        int lastComma = line.lastIndexOf(',');
        String ticker = line.substring(0, firstComma);
        String exchange = line.substring(lastComma + 1);
        String name = line.substring(firstComma + 1, lastComma);
        if (name.startsWith("\"") && name.endsWith("\"")) {
            name = name.substring(1, name.length() - 1);
        }
        return new Sp500Entry(ticker, name, exchange);
    }
}
