-- Normalized Schema for Stock Data

-- Table: exchanges
-- Stores information about stock exchanges (e.g., NYSE, NASDAQ)
CREATE TABLE IF NOT EXISTS exchanges (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    mic TEXT NOT NULL UNIQUE, -- Market Identifier Code (e.g., XNYS)
    name TEXT NOT NULL,
    currency TEXT NOT NULL,
    timezone TEXT NOT NULL
);

-- Table: symbols
-- Stores individual stock symbols/tickers linked to an exchange
CREATE TABLE IF NOT EXISTS symbols (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    exchange_id INTEGER NOT NULL,
    ticker TEXT NOT NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL, -- e.g., 'common', 'etf'
    FOREIGN KEY (exchange_id) REFERENCES exchanges(id),
    UNIQUE(exchange_id, ticker)
);

-- Table: daily_prices
-- Stores OHLCV (Open, High, Low, Close, Volume) data for symbols
CREATE TABLE IF NOT EXISTS daily_prices (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    symbol_id INTEGER NOT NULL,
    date TEXT NOT NULL, -- ISO8601 string YYYY-MM-DD
    open REAL NOT NULL,
    high REAL NOT NULL,
    low REAL NOT NULL,
    close REAL NOT NULL,
    volume INTEGER NOT NULL,
    adjusted_close REAL,
    FOREIGN KEY (symbol_id) REFERENCES symbols(id),
    UNIQUE(symbol_id, date)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_symbols_ticker ON symbols(ticker);
CREATE INDEX IF NOT EXISTS idx_daily_prices_date ON daily_prices(date);
CREATE INDEX IF NOT EXISTS idx_daily_prices_symbol_date ON daily_prices(symbol_id, date);
