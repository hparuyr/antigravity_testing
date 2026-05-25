INSERT INTO exchanges (mic, name, currency, timezone)
VALUES ('XNAS', 'NASDAQ', 'USD', 'America/New_York')
ON CONFLICT (mic) DO NOTHING;

INSERT INTO symbols (exchange_id, ticker, name, type)
SELECT id, 'AAPL', 'Apple Inc.', 'common'
FROM exchanges WHERE mic = 'XNAS'
ON CONFLICT (exchange_id, ticker) DO NOTHING;

INSERT INTO symbols (exchange_id, ticker, name, type)
SELECT id, 'GOOGL', 'Alphabet Inc.', 'common'
FROM exchanges WHERE mic = 'XNAS'
ON CONFLICT (exchange_id, ticker) DO NOTHING;

INSERT INTO daily_prices (symbol_id, date, open, high, low, close, volume)
SELECT s.id, '2023-10-25', 171.88, 173.06, 170.65, 171.10, 57157000
FROM symbols s WHERE s.ticker = 'AAPL'
ON CONFLICT (symbol_id, date) DO NOTHING;

INSERT INTO daily_prices (symbol_id, date, open, high, low, close, volume)
SELECT s.id, '2023-10-26', 170.37, 171.38, 165.67, 166.89, 70625300
FROM symbols s WHERE s.ticker = 'AAPL'
ON CONFLICT (symbol_id, date) DO NOTHING;

INSERT INTO daily_prices (symbol_id, date, open, high, low, close, volume)
SELECT s.id, '2023-10-27', 166.91, 168.96, 166.83, 168.22, 58499100
FROM symbols s WHERE s.ticker = 'AAPL'
ON CONFLICT (symbol_id, date) DO NOTHING;

INSERT INTO daily_prices (symbol_id, date, open, high, low, close, volume)
SELECT s.id, '2023-10-25', 138.50, 139.60, 136.60, 137.20, 32000000
FROM symbols s WHERE s.ticker = 'GOOGL'
ON CONFLICT (symbol_id, date) DO NOTHING;
