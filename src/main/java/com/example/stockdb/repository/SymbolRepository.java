package com.example.stockdb.repository;

import com.example.stockdb.model.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SymbolRepository extends JpaRepository<Symbol, Long> {
    Symbol findByTicker(String ticker);
    List<Symbol> findByExchangeId(Long exchangeId);
}
