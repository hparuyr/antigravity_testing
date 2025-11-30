package com.example.stockdb.repository;

import com.example.stockdb.model.IntradayPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntradayPriceRepository extends JpaRepository<IntradayPrice, Long> {
    List<IntradayPrice> findBySymbolId(Long symbolId);

    IntradayPrice findBySymbolIdAndTimestamp(Long symbolId, String timestamp);

    List<IntradayPrice> findBySymbolIdAndTimestampGreaterThanEqual(Long symbolId, String timestamp);
}
