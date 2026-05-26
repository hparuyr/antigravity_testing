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

No tests (`src/test/` is empty).

## Key env vars (all have defaults in `application.properties`)
| Var | Default | Notes |
|-----|---------|-------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/stock_db` | Neon connection string |
| `DB_USERNAME` | `postgres` | Fallback when URL lacks credentials |
| `DB_PASSWORD` | `postgres` | Fallback when URL lacks credentials |
| `STOCK_DATA_FETCHER` | `finnhub` | Active fetcher: `finnhub`, `alphavantage`, or `demo` (profile) |
| `STOCK_API_KEY` | `d8aq2u9r01qk20soblqgd8aq2u9r01qk20soblr0` | API key |
| `STOCK_API_URL` | `https://finnhub.io/api/v1` | API endpoint |
| `PORT` | `8080` | Server port |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated origins |

## API endpoints
All under `/api`; actuator health at `/actuator/health`.
| Method | Path | Notes |
|--------|------|-------|
| GET | `/` | Health/status |
| GET/POST | `/exchanges` | List / create |
| GET/POST | `/symbols` | List / create |
| GET | `/exchanges/{id}/symbols` | Symbols by exchange |
| GET | `/symbols/{id}/prices` | Daily prices |
| POST | `/prices` | Add price record |
| GET | `/daily/{ticker}?since=YYYY-MM-DD` | Daily prices since date |
| GET | `/intraday/{ticker}?since=YYYY-MM-DDTHH:mm:ss` | Intraday since timestamp |

## Scheduled tasks
- **Daily data**: daily at 6:00 AM (all S&P 500 tickers in DB)
- **Intraday data**: not implemented
- 15s delay between tickers (API rate limit)
- `ExchangeSymbolSeeder` creates NASDAQ (XNAS), NYSE (XNYS) + all 503 S&P 500 symbols on startup (`!demo` profile)
- `DemoDataSeeder` does the same for `demo` profile + fetches all price data

## Notable details
- `schema.sql` is SQLite syntax (`AUTOINCREMENT`) — reference only; JPA manages schema via `ddl-auto=update`
- `spring.jpa.open-in-view=false` — `@Transactional` required on write operations (used in `StockService`)
- CORS via `CorsConfig.java` — reads `cors.allowed.origins` env var, mapped on `/api/**`
- Stock data fetcher: `StockDataFetcher` interface, selected via `stock.data.fetcher` property (`finnhub`, `alphavantage`, or `demo` profile)
- `AlphaVantageService` and `FinnhubService` use the `RestTemplate` bean from `RestTemplateConfig` (injected)
- Docker healthcheck uses `wget http://localhost:8080/actuator/health`

## Data sources
- **S&P 500 list**: `src/main/resources/sp500.csv` — 503 tickers with names and exchange MICs
- **S&P 500 list loader**: `Sp500Loader` parses the CSV; `ExchangeSymbolSeeder` seeds exchanges + symbols on startup
- **Demo data**: `DemoStockDataFetcher` generates 365 days of synthetic OHLCV for every S&P 500 ticker
