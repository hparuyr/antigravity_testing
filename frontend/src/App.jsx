import './index.css';
import StockDashboard from './components/StockDashboard';

function App() {
  return (
    <div className="app">
      <header className="header">
        <h1>Stock Market Dashboard</h1>
        <p>Real-time stock data visualization</p>
      </header>
      <StockDashboard />
    </div>
  );
}

export default App;
