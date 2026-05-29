package com.example.stockdb.service;

import com.example.stockdb.dto.CreateExchangeRequest;
import com.example.stockdb.dto.CreateSymbolRequest;
import com.example.stockdb.dto.IngestRequest;
import com.example.stockdb.dto.IngestResult;
import com.example.stockdb.dto.IngestStatusResult;
import com.example.stockdb.exception.ExchangeNotFoundException;
import com.example.stockdb.exception.SymbolNotFoundException;
import com.example.stockdb.model.DailyPrice;
import com.example.stockdb.model.Exchange;
import com.example.stockdb.model.Symbol;
import com.example.stockdb.repository.DailyPriceRepository;
import com.example.stockdb.repository.ExchangeRepository;
import com.example.stockdb.repository.SymbolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StockService {

    private final ExchangeRepository exchangeRepository;
    private final SymbolRepository symbolRepository;
    private final DailyPriceRepository dailyPriceRepository;
    private final StockDataFetcher stockDataFetcher;

    public StockService(ExchangeRepository exchangeRepository, SymbolRepository symbolRepository,
                        DailyPriceRepository dailyPriceRepository, StockDataFetcher stockDataFetcher) {
        this.exchangeRepository = exchangeRepository;
        this.symbolRepository = symbolRepository;
        this.dailyPriceRepository = dailyPriceRepository;
        this.stockDataFetcher = stockDataFetcher;
    }

    public List<Exchange> getAllExchanges() {
        return exchangeRepository.findAll();
    }

    public Exchange getExchangeById(Long id) {
        return exchangeRepository.findById(id)
                .orElseThrow(() -> new ExchangeNotFoundException(id));
    }

    @Transactional
    public Exchange createExchange(CreateExchangeRequest request) {
        Exchange exchange = new Exchange();
        exchange.setMic(request.mic());
        exchange.setName(request.name());
        exchange.setCurrency(request.currency());
        exchange.setTimezone(request.timezone());
        return exchangeRepository.save(exchange);
    }

    public List<Symbol> getAllSymbols() {
        return symbolRepository.findAll();
    }

    public List<Symbol> getSymbolsByExchange(Long exchangeId) {
        if (!exchangeRepository.existsById(exchangeId)) {
            throw new ExchangeNotFoundException(exchangeId);
        }
        return symbolRepository.findByExchangeId(exchangeId);
    }

    @Transactional
    public Symbol createSymbol(CreateSymbolRequest request) {
        Exchange exchange = exchangeRepository.findById(request.exchangeId())
                .orElseThrow(() -> new ExchangeNotFoundException(request.exchangeId()));
        Symbol symbol = new Symbol();
        symbol.setExchange(exchange);
        symbol.setTicker(request.ticker());
        symbol.setName(request.name());
        symbol.setType(request.type());
        return symbolRepository.save(symbol);
    }

    public Symbol getSymbolByTicker(String ticker) {
        Symbol symbol = symbolRepository.findByTicker(ticker);
        if (symbol == null) {
            throw new SymbolNotFoundException(ticker);
        }
        return symbol;
    }

    public Symbol getSymbolById(Long id) {
        return symbolRepository.findById(id)
                .orElseThrow(() -> new SymbolNotFoundException("id:" + id));
    }

    @Transactional
    public List<DailyPrice> getPricesBySymbol(Long symbolId) {
        if (!symbolRepository.existsById(symbolId)) {
            throw new SymbolNotFoundException("id:" + symbolId);
        }
        return dailyPriceRepository.findBySymbolId(symbolId);
    }

    @Transactional
    public DailyPrice addPrice(DailyPrice price) {
        return dailyPriceRepository.save(price);
    }

    @Transactional
    public int fetchAndStoreDailyPrices(String ticker) {
        Symbol symbol = getSymbolByTicker(ticker);
        String outputSize = dailyPriceRepository.countBySymbolId(symbol.getId()) == 0
            ? StockDataFetcher.OUTPUT_SIZE_FULL
            : StockDataFetcher.OUTPUT_SIZE_COMPACT;

        List<DailyPrice> prices = stockDataFetcher.fetchDailyPrices(ticker, outputSize);
        for (DailyPrice price : prices) {
            price.setSymbol(symbol);
            upsertPrice(symbol.getId(), price);
        }
        return prices.size();
    }

    private void upsertPrice(Long symbolId, DailyPrice price) {
        DailyPrice existing = dailyPriceRepository.findBySymbolIdAndDate(symbolId, price.getDate());
        if (existing != null) {
            copyPriceData(price, existing);
            dailyPriceRepository.save(existing);
        } else {
            dailyPriceRepository.save(price);
        }
    }

    private void copyPriceData(DailyPrice from, DailyPrice to) {
        to.setOpen(from.getOpen());
        to.setHigh(from.getHigh());
        to.setLow(from.getLow());
        to.setClose(from.getClose());
        to.setVolume(from.getVolume());
        to.setAdjustedClose(from.getAdjustedClose());
    }

    public List<DailyPrice> getDailyPricesSince(String ticker, LocalDate since) {
        Symbol symbol = getSymbolByTicker(ticker);
        return dailyPriceRepository.findBySymbolIdAndDateGreaterThanEqualOrderByDateDesc(
                symbol.getId(), since.toString());
    }

    @Transactional
    public IngestResult ingestTicker(String ticker) {
        Symbol symbol = getSymbolByTicker(ticker);
        int countBefore = (int) dailyPriceRepository.countBySymbolId(symbol.getId());
        int fetched = fetchAndStoreDailyPrices(ticker);
        int countAfter = (int) dailyPriceRepository.countBySymbolId(symbol.getId());

        Optional<DailyPrice> first = dailyPriceRepository.findFirstBySymbolIdOrderByDateAsc(symbol.getId());
        Optional<DailyPrice> last = dailyPriceRepository.findFirstBySymbolIdOrderByDateDesc(symbol.getId());

        String status = fetched > 0 ? "loaded" : (countAfter > 0 ? "exists" : "empty");
        return new IngestResult(
                ticker,
                status,
                fetched,
                countAfter - countBefore,
                countAfter,
                first.map(DailyPrice::getDate).orElse(null),
                last.map(DailyPrice::getDate).orElse(null)
        );
    }

    @Transactional
    public List<IngestResult> ingestBatch(IngestRequest request) {
        List<String> tickers = request.tickers();
        if (request.all()) {
            tickers = symbolRepository.findAll().stream()
                    .map(Symbol::getTicker)
                    .toList();
        }
        List<IngestResult> results = new ArrayList<>();
        for (String ticker : tickers) {
            results.add(ingestTicker(ticker));
        }
        return results;
    }

    public IngestStatusResult getIngestStatus(String ticker) {
        Symbol symbol = symbolRepository.findByTicker(ticker);
        if (symbol == null) {
            return new IngestStatusResult(ticker, false, 0, null, null);
        }
        long count = dailyPriceRepository.countBySymbolId(symbol.getId());
        Optional<DailyPrice> first = dailyPriceRepository.findFirstBySymbolIdOrderByDateAsc(symbol.getId());
        Optional<DailyPrice> last = dailyPriceRepository.findFirstBySymbolIdOrderByDateDesc(symbol.getId());
        return new IngestStatusResult(
                ticker,
                true,
                (int) count,
                first.map(DailyPrice::getDate).orElse(null),
                last.map(DailyPrice::getDate).orElse(null)
        );
    }
}
