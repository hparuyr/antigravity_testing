package com.example.stockdb.repository;

import com.example.stockdb.model.DailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DailyPriceRepository extends JpaRepository<DailyPrice, Long> {
    List<DailyPrice> findBySymbolId(Long symbolId);

    List<DailyPrice> findBySymbolIdOrderByDateAsc(Long symbolId);

    DailyPrice findBySymbolIdAndDate(Long symbolId, String date);

    long countBySymbolId(Long symbolId);

    List<DailyPrice> findBySymbolIdAndDateGreaterThanEqualOrderByDateDesc(Long symbolId, String since);

    Optional<DailyPrice> findFirstBySymbolIdOrderByDateAsc(Long symbolId);

    Optional<DailyPrice> findFirstBySymbolIdOrderByDateDesc(Long symbolId);
}
