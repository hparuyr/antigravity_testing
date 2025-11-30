package com.example.stockdb.repository;

import com.example.stockdb.model.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    Exchange findByMic(String mic);
}
