# AGENTS.md — Stock Dashboard (TestGravity)

## Quick start
```bash
mvn spring-boot:run                                   # backend on :18080 (local profile, H2, no DB needed)
mvn spring-boot:run -Dspring-boot.run.profiles=local,demo  # seeds all S&P 500 with demo data (~2 min startup)

# Docker (production with PostgreSQL):
#   docker-compose up --build
```

## Architecture
- **Backend**: Spring Boot 3.3 / Java 17 / Maven (`pom.xml`)
- **Frontend**: React + Vite + Recharts (not present locally — `frontend/` does not exist in this repo)
- **Database**: PostgreSQL (Neon), JPA `ddl-auto=update`
- **Stock data**: Alpha Vantage API (free tier)
- **Deploy**: Docker (multi-stage) or Railway
- **Package**: `com.example.stockdb` — `model/`, `repository/`, `service/`, `controller/`, `config/`
- **Entrypoint**: `com.example.stockdb.StockDbApplication` (has `@EnableScheduling`)

## Commands (backend only)
| What | Command |
|------|---------|
| Run | `mvn spring-boot:run` |
| Build | `mvn clean package -DskipTests` |
| Docker | `docker-compose up --build` |

## Integration tests
```bash
export STOCK_API_KEY=your_key_here
mvn test -Dtest=FinnhubConnectionTest   # verifies Finnhub /quote (free tier)
mvn test -Dtest=AlphaVantageConnectionTest  # verifies Alpha Vantage daily prices (default)
```

## Key env vars (all have defaults in `application.properties`)
| Var | Default | Notes |
|-----|---------|-------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/stock_db` | Neon connection string |
| `DB_USERNAME` | `postgres` | Fallback when URL lacks credentials |
| `DB_PASSWORD` | `postgres` | Fallback when URL lacks credentials |
| `STOCK_DATA_FETCHER` | `alphavantage` | Active fetcher: `alphavantage` or `demo` (profile) |
| `STOCK_API_KEY` | — | API key (Alpha Vantage: get at https://www.alphavantage.co/support/#api-key) |
| `STOCK_API_URL` | `https://www.alphavantage.co/query` | API endpoint |
| `PORT` | `8080` | Server port |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated origins |
| `INTRADAY_ENABLED` | `false` | Enable Alpaca intraday WebSocket streaming |
| `ALPACA_API_KEY` | — | Alpaca API key ID |
| `ALPACA_API_SECRET` | — | Alpaca secret key |
| `ALPACA_WS_URL` | `wss://stream.data.alpaca.markets/v2/iex` | WebSocket URL (IEX free, `/v2/sip` paid) |

## API endpoints
All under `/api`; actuator health at `/actuator/health`.
Swagger UI at `http://localhost:18080/swagger-ui.html` (local) or `/swagger-ui.html` — fully documented with examples.

### Core stock data
| Method | Path | Notes |
|--------|------|-------|
| GET | `/` | Health/status |
| GET/POST | `/exchanges` | List / create |
| GET | `/exchanges/{id}` | Get exchange by ID |
| GET/POST | `/symbols` | List / create |
| GET | `/exchanges/{id}/symbols` | Symbols by exchange |
| GET | `/symbols/{id}/prices` | Daily prices |
| POST | `/prices` | Add price record |
| GET | `/daily/{ticker}?since=YYYY-MM-DD` | Daily prices since date (DB query, no API call) |
| GET | `/intraday/{ticker}?since=ISO_INSTANT` | Intraday minute bars (Alpaca WebSocket feed) |
| GET | `/intraday/{ticker}/latest` | Latest intraday bar for a ticker |
| GET | `/intraday/status` | WebSocket connection status |

### Ingestion (data loading)
| Method | Path | Notes |
|--------|------|-------|
| POST | `/ingest/{ticker}` | Fetch & store prices for one ticker from external API |
| POST | `/ingest` | Batch ingest (body: `{"tickers":["AAPL","MSFT"]}` or `{"all":true}`) |
| GET | `/ingest/{ticker}/status` | Check stored data for a ticker (no API call) |

### AI context
| Method | Path | Notes |
|--------|------|-------|
| GET | `/context/{ticker}?since=YYYY-MM-DD` | Complete stock profile: metadata + snapshot + 10 indicators + price history |

### Technical analysis (under `/api/analytics`)
| Method | Path | Notes |
|--------|------|-------|
| GET | `/{ticker}/returns?since=` | Daily simple & log returns |
| GET | `/{ticker}/sma?period=20&since=` | Simple Moving Average |
| GET | `/{ticker}/ema?period=20&since=` | Exponential Moving Average |
| GET | `/{ticker}/volatility?period=21&since=` | Rolling volatility |
| GET | `/{ticker}/rsi?period=14&since=` | Relative Strength Index |
| GET | `/{ticker}/macd?since=` | MACD line, signal, histogram |
| GET | `/{ticker}/bollinger?period=20&multiplier=2.0&since=` | Bollinger Bands |
| GET | `/{ticker}/vwap?since=` | Volume-Weighted Average Price |
| GET | `/correlation?ticker1=AAPL&ticker2=MSFT&since=` | Pairwise Pearson correlation |
| GET | `/{ticker}/beta?market=SPY&since=` | Beta, Alpha, R-squared |

### Stock similarity engine
| Method | Path | Notes |
|--------|------|-------|
| GET | `/{ticker}/mimics?since=&limit=20` | Find top-N most correlated S&P 500 stocks (server-side batch, single call) |

## Scheduled tasks
- **Daily data**: daily at 6:00 AM (all S&P 500 tickers in DB)
- **Intraday data**: real-time Alpaca WebSocket (`intraday.enabled=true`) — subscribes to all DB symbols on startup
- 15s delay between tickers (API rate limit)
- `ExchangeSymbolSeeder` creates NASDAQ (XNAS), NYSE (XNYS) + all 503 S&P 500 symbols on startup (`!demo` profile)
- `DemoDataSeeder` does the same for `demo` profile + fetches all price data

## Notable details
- `schema.sql` is SQLite syntax (`AUTOINCREMENT`) — reference only; JPA manages schema via `ddl-auto=update`
- `spring.jpa.open-in-view=false` — `@Transactional` required on write operations (used in `StockService`)
- CORS via `CorsConfig.java` — reads `cors.allowed.origins` env var, mapped on `/api/**`
- Stock data fetcher: `StockDataFetcher` interface, selected via `stock.data.fetcher` property (`alphavantage` or `demo` profile)
- `AlphaVantageService` uses the `RestTemplate` bean from `RestTemplateConfig` (injected)
- Finnhub `stock/candle` requires a paid plan; the free tier only provides `/quote` (current OHLC) and `/stock/profile2`
- Docker healthcheck uses `wget http://localhost:8080/actuator/health`
- **Swagger UI**: `springdoc-openapi-starter-webmvc-ui` (v2.6.0) — UI at `/swagger-ui.html`, spec at `/v3/api-docs`. All DTOs have `@Schema` examples for one-click "Try it out".

## Data sources
- **S&P 500 list**: `src/main/resources/sp500.csv` — 503 tickers with names and exchange MICs
- **S&P 500 list loader**: `Sp500Loader` parses the CSV; `ExchangeSymbolSeeder` seeds exchanges + symbols on startup
- **Demo data**: `DemoStockDataFetcher` generates 365 days of synthetic OHLCV for every S&P 500 ticker
