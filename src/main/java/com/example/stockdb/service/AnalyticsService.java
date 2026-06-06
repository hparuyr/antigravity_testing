package com.example.stockdb.service;

import com.example.stockdb.dto.*;
import com.example.stockdb.exception.SymbolNotFoundException;
import com.example.stockdb.model.DailyPrice;
import com.example.stockdb.model.Symbol;
import com.example.stockdb.repository.DailyPriceRepository;
import com.example.stockdb.repository.SymbolRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final SymbolRepository symbolRepository;
    private final DailyPriceRepository dailyPriceRepository;

    public AnalyticsService(SymbolRepository symbolRepository, DailyPriceRepository dailyPriceRepository) {
        this.symbolRepository = symbolRepository;
        this.dailyPriceRepository = dailyPriceRepository;
    }

    private List<DailyPrice> getPricesAscending(String ticker) {
        var symbol = symbolRepository.findByTicker(ticker);
        if (symbol == null)
            throw new SymbolNotFoundException(ticker);
        return dailyPriceRepository.findBySymbolIdOrderByDateAsc(symbol.getId());
    }

    public List<ReturnPoint> getDailyReturns(String ticker, LocalDate since) {
        var prices = getPricesAscending(ticker);
        if (prices.size() < 2) return List.of();

        String sinceStr = since.toString();
        var result = new ArrayList<ReturnPoint>();
        for (int i = 1; i < prices.size(); i++) {
            if (prices.get(i).getDate().compareTo(sinceStr) < 0) continue;
            double prev = prices.get(i - 1).getClose();
            double curr = prices.get(i).getClose();
            result.add(new ReturnPoint(
                    prices.get(i).getDate(),
                    (curr - prev) / prev,
                    Math.log(curr / prev)));
        }
        return result;
    }

    public List<MovingAveragePoint> getSMA(String ticker, int period, LocalDate since) {
        var prices = getPricesAscending(ticker);
        if (prices.size() < period) return List.of();

        String sinceStr = since.toString();
        var result = new ArrayList<MovingAveragePoint>();
        for (int i = period - 1; i < prices.size(); i++) {
            if (prices.get(i).getDate().compareTo(sinceStr) < 0) continue;
            double sum = 0;
            for (int j = i - period + 1; j <= i; j++)
                sum += prices.get(j).getClose();
            result.add(new MovingAveragePoint(prices.get(i).getDate(), sum / period));
        }
        return result;
    }

    public List<MovingAveragePoint> getEMA(String ticker, int period, LocalDate since) {
        var prices = getPricesAscending(ticker);
        if (prices.size() < period) return List.of();

        double multiplier = 2.0 / (period + 1);
        double sum = 0;
        for (int i = 0; i < period; i++)
            sum += prices.get(i).getClose();
        double ema = sum / period;

        String sinceStr = since.toString();
        var result = new ArrayList<MovingAveragePoint>();
        if (prices.get(period - 1).getDate().compareTo(sinceStr) >= 0)
            result.add(new MovingAveragePoint(prices.get(period - 1).getDate(), ema));

        for (int i = period; i < prices.size(); i++) {
            ema = (prices.get(i).getClose() - ema) * multiplier + ema;
            if (prices.get(i).getDate().compareTo(sinceStr) >= 0)
                result.add(new MovingAveragePoint(prices.get(i).getDate(), ema));
        }
        return result;
    }

    public List<VolatilityPoint> getVolatility(String ticker, int period, LocalDate since) {
        var returns = getDailyReturns(ticker, LocalDate.of(1900, 1, 1));
        if (returns.size() < period) return List.of();

        String sinceStr = since.toString();
        var result = new ArrayList<VolatilityPoint>();
        for (int i = period - 1; i < returns.size(); i++) {
            if (returns.get(i).date().compareTo(sinceStr) < 0) continue;
            double mean = 0;
            for (int j = i - period + 1; j <= i; j++)
                mean += returns.get(j).logReturn();
            mean /= period;

            double variance = 0;
            for (int j = i - period + 1; j <= i; j++) {
                double diff = returns.get(j).logReturn() - mean;
                variance += diff * diff;
            }
            result.add(new VolatilityPoint(returns.get(i).date(), Math.sqrt(variance / (period - 1))));
        }
        return result;
    }

    public List<RSIPoint> getRSI(String ticker, int period, LocalDate since) {
        var prices = getPricesAscending(ticker);
        if (prices.size() < period + 1) return List.of();

        String sinceStr = since.toString();
        var result = new ArrayList<RSIPoint>();
        for (int i = period; i < prices.size(); i++) {
            if (prices.get(i).getDate().compareTo(sinceStr) < 0) continue;
            double avgGain = 0, avgLoss = 0;
            for (int j = i - period + 1; j <= i; j++) {
                double change = prices.get(j).getClose() - prices.get(j - 1).getClose();
                if (change >= 0) avgGain += change;
                else avgLoss -= change;
            }
            avgGain /= period;
            avgLoss /= period;

            double rsi = 100;
            if (avgLoss > 0) {
                double rs = avgGain / avgLoss;
                rsi = 100 - (100 / (1 + rs));
            } else if (avgGain > 0) {
                rsi = 100;
            }
            result.add(new RSIPoint(prices.get(i).getDate(), rsi));
        }
        return result;
    }

    public List<MACDPoint> getMACD(String ticker, LocalDate since) {
        var prices = getPricesAscending(ticker);
        int fast = 12, slow = 26, signal = 9;
        if (prices.size() < slow + signal) return List.of();

        var ema12 = calcEMA(prices, fast);
        var ema26 = calcEMA(prices, slow);

        var macdLine = new ArrayList<Double>();
        var macdDates = new ArrayList<String>();
        for (int i = slow - 1; i < prices.size(); i++) {
            macdLine.add(ema12.get(i - fast + 1) - ema26.get(i - slow + 1));
            macdDates.add(prices.get(i).getDate());
        }

        double signalMul = 2.0 / (signal + 1);
        double sigSum = 0;
        for (int i = 0; i < signal; i++)
            sigSum += macdLine.get(i);
        double sigVal = sigSum / signal;

        String sinceStr = since.toString();
        var result = new ArrayList<MACDPoint>();
        for (int i = signal - 1; i < macdLine.size(); i++) {
            if (i >= signal)
                sigVal = (macdLine.get(i) - sigVal) * signalMul + sigVal;
            if (macdDates.get(i).compareTo(sinceStr) >= 0)
                result.add(new MACDPoint(macdDates.get(i), macdLine.get(i), sigVal, macdLine.get(i) - sigVal));
        }
        return result;
    }

    private List<Double> calcEMA(List<DailyPrice> prices, int period) {
        double multiplier = 2.0 / (period + 1);
        double sum = 0;
        for (int i = 0; i < period; i++)
            sum += prices.get(i).getClose();
        double ema = sum / period;
        var result = new ArrayList<Double>();
        result.add(ema);
        for (int i = period; i < prices.size(); i++) {
            ema = (prices.get(i).getClose() - ema) * multiplier + ema;
            result.add(ema);
        }
        return result;
    }

    public List<BollingerPoint> getBollingerBands(String ticker, int period, double multiplier, LocalDate since) {
        var prices = getPricesAscending(ticker);
        if (prices.size() < period) return List.of();

        String sinceStr = since.toString();
        var result = new ArrayList<BollingerPoint>();
        for (int i = period - 1; i < prices.size(); i++) {
            if (prices.get(i).getDate().compareTo(sinceStr) < 0) continue;
            double sum = 0;
            for (int j = i - period + 1; j <= i; j++)
                sum += prices.get(j).getClose();
            double middle = sum / period;

            double variance = 0;
            for (int j = i - period + 1; j <= i; j++) {
                double diff = prices.get(j).getClose() - middle;
                variance += diff * diff;
            }
            double std = Math.sqrt(variance / period);
            result.add(new BollingerPoint(
                    prices.get(i).getDate(),
                    middle + multiplier * std,
                    middle,
                    middle - multiplier * std));
        }
        return result;
    }

    public List<VWAPPoint> getVWAP(String ticker, LocalDate since) {
        var prices = getPricesAscending(ticker);
        if (prices.isEmpty()) return List.of();

        String sinceStr = since.toString();
        var result = new ArrayList<VWAPPoint>();
        double cumTPV = 0, cumVol = 0;
        for (var p : prices) {
            double typical = (p.getHigh() + p.getLow() + p.getClose()) / 3.0;
            cumTPV += typical * p.getVolume();
            cumVol += p.getVolume();
            if (p.getDate().compareTo(sinceStr) >= 0)
                result.add(new VWAPPoint(p.getDate(), cumTPV / cumVol));
        }
        return result;
    }

    public CorrelationResult getCorrelation(String ticker1, String ticker2, LocalDate since) {
        var r1 = getDailyReturns(ticker1, LocalDate.of(1900, 1, 1));
        var r2 = getDailyReturns(ticker2, LocalDate.of(1900, 1, 1));

        var m1 = r1.stream().collect(Collectors.toMap(ReturnPoint::date, ReturnPoint::logReturn));
        var m2 = r2.stream().collect(Collectors.toMap(ReturnPoint::date, ReturnPoint::logReturn));

        String sinceStr = since.toString();
        var aligned = new ArrayList<Map.Entry<Double, Double>>();
        for (var e : m1.entrySet()) {
            if (e.getKey().compareTo(sinceStr) < 0) continue;
            var v2 = m2.get(e.getKey());
            if (v2 != null)
                aligned.add(Map.entry(e.getValue(), v2));
        }

        if (aligned.size() < 2)
            return new CorrelationResult(ticker1, ticker2, null, 0);

        double mean1 = 0, mean2 = 0;
        for (var p : aligned) { mean1 += p.getKey(); mean2 += p.getValue(); }
        mean1 /= aligned.size();
        mean2 /= aligned.size();

        double covar = 0, var1 = 0, var2 = 0;
        for (var p : aligned) {
            double d1 = p.getKey() - mean1;
            double d2 = p.getValue() - mean2;
            covar += d1 * d2;
            var1 += d1 * d1;
            var2 += d2 * d2;
        }

        double denom = Math.sqrt(var1 * var2);
        double corr = denom > 0 ? covar / denom : 0;
        return new CorrelationResult(ticker1, ticker2, corr, aligned.size());
    }

    public BetaResult getBeta(String ticker, String market, LocalDate since) {
        var stockR = getDailyReturns(ticker, LocalDate.of(1900, 1, 1));
        var marketR = getDailyReturns(market, LocalDate.of(1900, 1, 1));

        var stockMap = stockR.stream().collect(Collectors.toMap(ReturnPoint::date, ReturnPoint::logReturn));
        var marketMap = marketR.stream().collect(Collectors.toMap(ReturnPoint::date, ReturnPoint::logReturn));

        String sinceStr = since.toString();
        var aligned = new ArrayList<Map.Entry<Double, Double>>();
        for (var e : stockMap.entrySet()) {
            if (e.getKey().compareTo(sinceStr) < 0) continue;
            var mv = marketMap.get(e.getKey());
            if (mv != null)
                aligned.add(Map.entry(e.getValue(), mv));
        }

        if (aligned.size() < 2)
            return new BetaResult(ticker, market, null, null, null, 0);

        double meanS = 0, meanM = 0;
        for (var p : aligned) { meanS += p.getKey(); meanM += p.getValue(); }
        meanS /= aligned.size();
        meanM /= aligned.size();

        double covar = 0, varM = 0;
        for (var p : aligned) {
            double ds = p.getKey() - meanS;
            double dm = p.getValue() - meanM;
            covar += ds * dm;
            varM += dm * dm;
        }

        double beta = varM > 0 ? covar / varM : 0;
        double alpha = meanS - beta * meanM;

        double varS = 0;
        for (var p : aligned) {
            double ds = p.getKey() - meanS;
            varS += ds * ds;
        }
        double rSquared = varS > 0 ? (beta * beta * varM) / varS : 0;

        return new BetaResult(ticker, market, beta, alpha, rSquared, aligned.size());
    }

    public MimicsResult findMimics(String ticker, LocalDate since, int limit) {
        Symbol targetSymbol = symbolRepository.findByTicker(ticker);
        if (targetSymbol == null) throw new SymbolNotFoundException(ticker);

        List<Symbol> allSymbols = symbolRepository.findAll();
        Map<String, Symbol> symbolByTicker = allSymbols.stream()
                .collect(Collectors.toMap(Symbol::getTicker, s -> s));

        Map<String, Double> targetReturns = returnsMap(ticker, since);

        List<MimicEntry> candidates = new ArrayList<>();
        for (Symbol candidate : allSymbols) {
            if (candidate.getTicker().equals(ticker)) continue;

            Map<String, Double> candReturns = returnsMap(candidate.getTicker(), since);

            double[] aligned = alignReturns(targetReturns, candReturns);
            if (aligned == null || (int) aligned[0] < 10) continue;

            int n = (int) aligned[0];
            double corr = aligned[1];
            double beta = computeBetaFromSums(aligned, n);
            double r2 = computeRSquaredFromSums(aligned, n, beta);
            double vol = aligned[6];

            candidates.add(new MimicEntry(0, candidate.getTicker(), candidate.getName(),
                    candidate.getExchange().getMic(), corr, beta, r2, vol));
        }

        candidates.sort((a, b) -> Double.compare(b.correlation(), a.correlation()));
        if (limit > 0 && limit < candidates.size()) {
            candidates = candidates.subList(0, limit);
        }

        List<MimicEntry> ranked = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            var c = candidates.get(i);
            ranked.add(new MimicEntry(i + 1, c.ticker(), c.name(), c.exchange(),
                    c.correlation(), c.beta(), c.rSquared(), c.volatility()));
        }

        return new MimicsResult(ticker, since.toString(), allSymbols.size() - 1, ranked);
    }

    private Map<String, Double> returnsMap(String ticker, LocalDate since) {
        List<DailyPrice> prices = getPricesAscending(ticker);
        Map<String, Double> map = new LinkedHashMap<>();
        String sinceStr = since.toString();
        for (int i = 1; i < prices.size(); i++) {
            if (prices.get(i).getDate().compareTo(sinceStr) < 0) continue;
            map.put(prices.get(i).getDate(),
                    Math.log(prices.get(i).getClose() / prices.get(i - 1).getClose()));
        }
        return map;
    }

    private double[] alignReturns(Map<String, Double> target, Map<String, Double> candidate) {
        List<Double> tVals = new ArrayList<>();
        List<Double> cVals = new ArrayList<>();
        for (var e : target.entrySet()) {
            Double cv = candidate.get(e.getKey());
            if (cv != null) {
                tVals.add(e.getValue());
                cVals.add(cv);
            }
        }
        int n = tVals.size();
        if (n < 2) return null;

        double sumT = 0, sumC = 0, sumTT = 0, sumCC = 0, sumTC = 0;
        for (int i = 0; i < n; i++) {
            double t = tVals.get(i), c = cVals.get(i);
            sumT += t; sumC += c;
            sumTT += t * t; sumCC += c * c; sumTC += t * c;
        }
        double meanC = sumC / n;
        double varC = 0;
        for (int i = 0; i < n; i++) {
            double d = cVals.get(i) - meanC;
            varC += d * d;
        }

        double denom = Math.sqrt((n * sumTT - sumT * sumT) * (n * sumCC - sumC * sumC));
        double corr = denom == 0 ? 0 : (n * sumTC - sumT * sumC) / denom;

        return new double[]{n, corr, sumT, sumC, sumTT, sumCC, Math.sqrt(varC / (n - 1)), sumTC};
    }

    private double computeBetaFromSums(double[] s, int n) {
        double beta = (n * s[7] - s[2] * s[3]) / (n * s[5] - s[3] * s[3]);
        return Double.isFinite(beta) ? beta : 0;
    }

    private double computeRSquaredFromSums(double[] s, int n, double beta) {
        double varM = (s[5] - s[3] * s[3] / n) / (n - 1);
        double varS = (s[4] - s[2] * s[2] / n) / (n - 1);
        return varS > 0 ? (beta * beta * varM) / varS : 0;
    }
}
