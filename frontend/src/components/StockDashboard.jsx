import { useState, useEffect } from 'react';
import { LineChart, Line, AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const TIME_RANGES = [
    { value: '5min', label: 'Last 5 Minutes', type: 'intraday' },
    { value: '1hour', label: 'Last 1 Hour', type: 'intraday' },
    { value: '12hours', label: 'Last 12 Hours', type: 'intraday' },
    { value: '1day', label: 'Last 1 Day', type: 'intraday' },
    { value: '1week', label: 'Last 1 Week', type: 'daily' },
    { value: '1month', label: 'Last 1 Month', type: 'daily' },
];

const CHART_COLORS = [
    '#a78bfa', // purple
    '#60a5fa', // blue
    '#34d399', // green
    '#fbbf24', // yellow
    '#f87171', // red
    '#ec4899', // pink
];

function StockDashboard() {
    const [symbols, setSymbols] = useState([]);
    const [selectedSymbols, setSelectedSymbols] = useState([]);
    const [timeRange, setTimeRange] = useState('1day');
    const [chartData, setChartData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchSymbols();
    }, []);

    const fetchSymbols = async () => {
        try {
            const response = await fetch(`${API_BASE}/symbols`);
            const symbolsData = await response.json();
            setSymbols(symbolsData);
        } catch (err) {
            console.error('Error fetching symbols:', err);
            setError('Failed to load symbols');
        }
    };

    const handleSymbolToggle = (ticker) => {
        setSelectedSymbols(prev => {
            if (prev.includes(ticker)) {
                return prev.filter(t => t !== ticker);
            } else {
                return [...prev, ticker];
            }
        });
    };

    const loadData = async () => {
        if (selectedSymbols.length === 0) {
            setError('Please select at least one symbol');
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const range = TIME_RANGES.find(r => r.value === timeRange);
            const now = new Date();

            // Fetch data for all selected symbols
            const dataPromises = selectedSymbols.map(async (ticker) => {
                let url = '';

                if (range.type === 'intraday') {
                    let since = new Date(now);
                    if (timeRange === '5min') since.setMinutes(since.getMinutes() - 5);
                    if (timeRange === '1hour') since.setHours(since.getHours() - 1);
                    if (timeRange === '12hours') since.setHours(since.getHours() - 12);
                    if (timeRange === '1day') since.setDate(since.getDate() - 1);

                    const sinceStr = toLocalIsoString(since);
                    url = `${API_BASE}/intraday/${ticker}?since=${sinceStr}`;
                } else {
                    let since = new Date(now);
                    if (timeRange === '1week') since.setDate(since.getDate() - 7);
                    if (timeRange === '1month') since.setMonth(since.getMonth() - 1);

                    const sinceStr = since.toISOString().split('T')[0];
                    url = `${API_BASE}/daily/${ticker}?since=${sinceStr}`;
                }

                const response = await fetch(url);
                const data = await response.json();
                return { ticker, data };
            });

            const results = await Promise.all(dataPromises);

            // Merge data from all stocks into a single timeline
            const timeMap = new Map();

            results.forEach(({ ticker, data }) => {
                data.forEach(item => {
                    const time = item.timestamp || item.date;
                    if (!timeMap.has(time)) {
                        timeMap.set(time, { time });
                    }
                    const entry = timeMap.get(time);
                    entry[`${ticker}_close`] = item.close;
                });
            });

            const mergedData = Array.from(timeMap.values())
                .sort((a, b) => a.time.localeCompare(b.time));

            if (mergedData.length === 0) {
                setError(`No data available for the selected time range. The database may not have ${range.type} data for these stocks yet. Try selecting "Last 1 Week" or "Last 1 Month" for daily data.`);
                setChartData([]);
                return;
            }

            setChartData(mergedData);
        } catch (err) {
            console.error('Error loading data:', err);
            setError('Failed to load stock data');
        } finally {
            setLoading(false);
        }
    };

    const toLocalIsoString = (date) => {
        const pad = (num) => (num < 10 ? '0' : '') + num;
        return date.getFullYear() +
            '-' + pad(date.getMonth() + 1) +
            '-' + pad(date.getDate()) +
            'T' + pad(date.getHours()) +
            ':' + pad(date.getMinutes()) +
            ':' + pad(date.getSeconds());
    };

    const formatTime = (time) => {
        if (time.includes('T')) {
            return new Date(time).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
        }
        return time;
    };

    const CustomTooltip = ({ active, payload }) => {
        if (active && payload && payload.length) {
            return (
                <div style={{
                    background: 'rgba(0, 0, 0, 0.9)',
                    padding: '12px',
                    borderRadius: '8px',
                    border: '1px solid rgba(255, 255, 255, 0.2)'
                }}>
                    <p style={{ color: '#fff', marginBottom: '8px', fontWeight: 'bold' }}>
                        {payload[0].payload.time}
                    </p>
                    {payload.map((entry, index) => (
                        <p key={index} style={{ color: entry.color, margin: '4px 0' }}>
                            {entry.name}: ${entry.value?.toFixed(2)}
                        </p>
                    ))}
                </div>
            );
        }
        return null;
    };

    return (
        <div className="dashboard">
            <div className="controls">
                <div className="control-section">
                    <h3>Select Stocks</h3>
                    <div className="checkbox-grid">
                        {symbols.map(s => (
                            <label key={s.id} className="checkbox-label">
                                <input
                                    type="checkbox"
                                    checked={selectedSymbols.includes(s.ticker)}
                                    onChange={() => handleSymbolToggle(s.ticker)}
                                />
                                <span>{s.ticker}</span>
                            </label>
                        ))}
                    </div>
                </div>

                <div className="controls-grid">
                    <div className="control-group">
                        <label htmlFor="timeRange">Time Range</label>
                        <select
                            id="timeRange"
                            value={timeRange}
                            onChange={(e) => setTimeRange(e.target.value)}
                        >
                            {TIME_RANGES.map(range => (
                                <option key={range.value} value={range.value}>
                                    {range.label}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="control-group">
                        <label>&nbsp;</label>
                        <button onClick={loadData}>Load Data</button>
                    </div>
                </div>
            </div>

            <div className="chart-container">
                <div className="chart-header">
                    <h2>
                        {selectedSymbols.length > 0
                            ? `${selectedSymbols.join(', ')} Price Comparison`
                            : 'Stock Price Chart'}
                    </h2>
                </div>

                {loading && <div className="loading">Loading data...</div>}
                {error && <div className="error">{error}</div>}
                {!loading && !error && chartData.length === 0 && (
                    <div className="no-data">Select stocks and click Load Data to view the chart.</div>
                )}
                {!loading && !error && chartData.length > 0 && (
                    <div className="chart-wrapper">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
                                <XAxis
                                    dataKey="time"
                                    tickFormatter={formatTime}
                                    stroke="rgba(255,255,255,0.6)"
                                    tick={{ fill: 'rgba(255,255,255,0.6)' }}
                                />
                                <YAxis
                                    stroke="rgba(255,255,255,0.6)"
                                    tick={{ fill: 'rgba(255,255,255,0.6)' }}
                                    domain={['auto', 'auto']}
                                />
                                <Tooltip content={<CustomTooltip />} />
                                <Legend wrapperStyle={{ color: 'rgba(255,255,255,0.8)' }} />
                                {selectedSymbols.map((ticker, index) => (
                                    <Line
                                        key={ticker}
                                        type="monotone"
                                        dataKey={`${ticker}_close`}
                                        stroke={CHART_COLORS[index % CHART_COLORS.length]}
                                        strokeWidth={2}
                                        dot={false}
                                        name={`${ticker} Close`}
                                        connectNulls
                                    />
                                ))}
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                )}
            </div>
        </div>
    );
}

export default StockDashboard;
