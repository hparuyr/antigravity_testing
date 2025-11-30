# Stock Dashboard - Railway Deployment

This project is configured for easy deployment to Railway.

## Quick Deploy to Railway

[![Deploy on Railway](https://railway.app/button.svg)](https://railway.app/new/template)

## Manual Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed deployment instructions.

## Project Structure

```
TestGravity/
├── src/                    # Spring Boot backend
├── frontend/              # React frontend
├── Dockerfile            # Backend Docker configuration
├── frontend/Dockerfile   # Frontend Docker configuration
└── DEPLOYMENT.md        # Deployment guide
```

## Local Development

### Backend
```bash
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

## Environment Variables

### Backend
- `STOCK_API_KEY` - Alpha Vantage API key
- `DATABASE_URL` - PostgreSQL connection string (auto-set by Railway)
- `PORT` - Server port (default: 8080)

### Frontend
- `VITE_API_URL` - Backend API URL

## Features

- 📊 Multi-stock comparison charts
- 📈 Intraday and daily data support
- 🎨 Premium dark mode UI
- 🔄 Automatic data fetching
- 📱 Responsive design

## Tech Stack

- **Backend**: Spring Boot, PostgreSQL, Alpha Vantage API
- **Frontend**: React, Vite, Recharts
- **Deployment**: Railway, Docker
