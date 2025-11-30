package com.example.stockdb.repository;

import com.example.stockdb.model.DailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyPriceRepository extends JpaRepository<DailyPrice, Long> {
    List<DailyPrice> findBySymbolId(Long symbolId);
    DailyPrice findBySymbolIdAndDate(Long symbolId, String date);
}
