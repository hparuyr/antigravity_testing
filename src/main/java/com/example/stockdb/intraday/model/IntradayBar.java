package com.example.stockdb.intraday.model;

import com.example.stockdb.model.Symbol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "intraday_bars", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"symbol_id", "timestamp"})
})
@Getter @Setter
public class IntradayBar {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_seq")
    @SequenceGenerator(name = "global_seq", sequenceName = "global_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "symbol_id", nullable = false)
    private Symbol symbol;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private Double open;

    @Column(nullable = false)
    private Double high;

    @Column(nullable = false)
    private Double low;

    @Column(nullable = false)
    private Double close;

    @Column(nullable = false)
    private Long volume;

    @Column(name = "trade_count")
    private Integer tradeCount;

    private Double vwap;
}
