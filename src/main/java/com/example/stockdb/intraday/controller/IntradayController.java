package com.example.stockdb.intraday.controller;

import com.example.stockdb.intraday.model.IntradayBar;
import com.example.stockdb.intraday.service.IntradayService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/intraday")
@ConditionalOnBean(name = "intradayService")
public class IntradayController {

    private final IntradayService intradayService;

    public IntradayController(IntradayService intradayService) {
        this.intradayService = intradayService;
    }

    @GetMapping("/{ticker}")
    public ResponseEntity<?> getBarsSince(
            @PathVariable String ticker,
            @RequestParam(name = "since", required = false) String since) {
        try {
            Instant sinceInstant = since != null ? Instant.parse(since) : Instant.now().minusSeconds(3600);
            List<IntradayBar> bars = intradayService.getBarsSince(ticker.toUpperCase(), sinceInstant);
            return ResponseEntity.ok(bars);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{ticker}/latest")
    public ResponseEntity<?> getLatest(@PathVariable String ticker) {
        IntradayBar bar = intradayService.getLatestBar(ticker.toUpperCase());
        if (bar == null) {
            return ResponseEntity.ok(Map.of("status", "no_data", "ticker", ticker.toUpperCase()));
        }
        return ResponseEntity.ok(bar);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "connected", intradayService.isConnected()
        ));
    }
}
