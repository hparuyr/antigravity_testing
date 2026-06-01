package com.example.stockdb.intraday.repository;

import com.example.stockdb.intraday.model.IntradayBar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface IntradayBarRepository extends JpaRepository<IntradayBar, Long> {

    List<IntradayBar> findBySymbolIdAndTimestampAfterOrderByTimestampAsc(Long symbolId, Instant since);

    List<IntradayBar> findBySymbolIdOrderByTimestampDesc(Long symbolId);

    Optional<IntradayBar> findTopBySymbolIdOrderByTimestampDesc(Long symbolId);

    Optional<IntradayBar> findBySymbolIdAndTimestamp(Long symbolId, Instant timestamp);

    long countBySymbolId(Long symbolId);

    void deleteBySymbolId(Long symbolId);
}
