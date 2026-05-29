package com.example.stockdb.exception;

public class SymbolNotFoundException extends RuntimeException {
    private final String ticker;

    public SymbolNotFoundException(String ticker) {
        super("Symbol not found: " + ticker);
        this.ticker = ticker;
    }

    public String getTicker() {
        return ticker;
    }
}
