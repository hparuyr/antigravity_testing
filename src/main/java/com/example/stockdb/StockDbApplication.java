package com.example.stockdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StockDbApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockDbApplication.class, args);
    }

}
