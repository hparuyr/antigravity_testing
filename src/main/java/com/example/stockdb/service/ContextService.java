package com.example.stockdb.service;

import com.example.stockdb.dto.*;
import com.example.stockdb.exception.SymbolNotFoundException;
import com.example.stockdb.model.DailyPrice;
import com.example.stockdb.model.Symbol;
import com.example.stockdb.repository.DailyPriceRepository;
import com.example.stockdb.repository.SymbolRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ContextService {

    private final SymbolRepository symbolRepository;
    private final DailyPriceRepository dailyPriceRepository;
    private final AnalyticsService analyticsService;

    public ContextService(SymbolRepository symbolRepository,
                          DailyPriceRepository dailyPriceRepository,
                          AnalyticsService analyticsService) {
        this.symbolRepository = symbolRepository;
        this.dailyPriceRepository = dailyPriceRepository;
        this.analyticsService = analyticsService;
    }

    public StockContext getStockContext(String ticker, LocalDate since) {
        Symbol symbol = symbolRepository.findByTicker(ticker);
        if (symbol == null) throw new SymbolNotFoundException(ticker);

        List<DailyPrice> prices = dailyPriceRepository
                .findBySymbolIdAndDateGreaterThanEqualOrderByDateDesc(symbol.getId(), since.toString());

        if (prices.isEmpty()) {
            return new StockContext(ticker, symbol.getName(), symbol.getExchange().getMic(),
                    null, null, 0, null, null, List.of());
        }

        String from = prices.get(prices.size() - 1).getDate();
        String to = prices.get(0).getDate();

        DailyPrice latest = prices.get(0);
        DailyPrice prev = prices.size() > 1 ? prices.get(1) : latest;
        double change = latest.getClose() - prev.getClose();

        double sumVol = 0;
        for (var p : prices) sumVol += p.getVolume();
        long avgVol = (long) (sumVol / prices.size());

        List<DailyPrice> yearly = dailyPriceRepository
                .findBySymbolIdAndDateGreaterThanEqualOrderByDateDesc(symbol.getId(),
                        LocalDate.now().minusYears(1).toString());
        double low52w = yearly.stream().mapToDouble(DailyPrice::getLow).min().orElse(latest.getLow());
        double high52w = yearly.stream().mapToDouble(DailyPrice::getHigh).max().orElse(latest.getHigh());

        PriceSnapshot snapshot = new PriceSnapshot(latest.getClose(), change,
                prev.getClose() > 0 ? change / prev.getClose() : 0, high52w, low52w, avgVol);

        LocalDate yearAgo = LocalDate.now().minusYears(1);
        var sma20 = lastVal(analyticsService.getSMA(ticker, 20, since), MovingAveragePoint::value);
        var sma50 = lastVal(analyticsService.getSMA(ticker, 50, since), MovingAveragePoint::value);
        var ema12 = lastVal(analyticsService.getEMA(ticker, 12, since), MovingAveragePoint::value);
        var ema26 = lastVal(analyticsService.getEMA(ticker, 26, since), MovingAveragePoint::value);
        var rsi14 = lastVal(analyticsService.getRSI(ticker, 14, since), RSIPoint::rsi);
        var vol21 = lastVal(analyticsService.getVolatility(ticker, 21, since), VolatilityPoint::volatility);

        var macdList = analyticsService.getMACD(ticker, since);
        MacdSnapshot macdSnap = null;
        if (!macdList.isEmpty()) {
            var m = macdList.get(macdList.size() - 1);
            macdSnap = new MacdSnapshot(m.macd(), m.signal(), m.histogram());
        }

        var bollList = analyticsService.getBollingerBands(ticker, 20, 2.0, since);
        BollingerSnapshot bollSnap = null;
        if (!bollList.isEmpty()) {
            var b = bollList.get(bollList.size() - 1);
            bollSnap = new BollingerSnapshot(b.upper(), b.middle(), b.lower());
        }

        IndicatorSnapshot indicators = new IndicatorSnapshot(
                sma20, sma50, ema12, ema26, rsi14,
                macdSnap, bollSnap, vol21,
                analyticsService.getBeta(ticker, "SPY", yearAgo).beta(),
                analyticsService.getCorrelation(ticker, "SPY", yearAgo).correlation()
        );

        return new StockContext(ticker, symbol.getName(), symbol.getExchange().getMic(),
                from, to, prices.size(), snapshot, indicators, prices);
    }

    private <T> Double lastVal(List<T> list, java.util.function.ToDoubleFunction<T> extractor) {
        if (list.isEmpty()) return null;
        return extractor.applyAsDouble(list.get(list.size() - 1));
    }
}
