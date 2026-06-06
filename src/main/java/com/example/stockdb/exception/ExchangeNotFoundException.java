package com.example.stockdb.exception;

public class ExchangeNotFoundException extends RuntimeException {
    private final Long id;

    public ExchangeNotFoundException(Long id) {
        super("Exchange not found: " + id);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
