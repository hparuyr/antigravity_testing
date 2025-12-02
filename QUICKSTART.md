# Quick Start Guide

## Step 1: Configure Your Neon Database Credentials

I've created a `.env` file for you. You need to update it with your actual Neon database credentials:

1. **Open the `.env` file** in your project root
2. **Get your Neon credentials** from [Neon Console](https://console.neon.tech):
   - Go to your project dashboard
   - Click on "Connection Details" or "Connect"
   - Copy the connection string

3. **Update the `.env` file** with your actual values:
   ```env
   DATABASE_URL=postgresql://your_actual_username:your_actual_password@ep-xxxxx.region.aws.neon.tech/your_dbname?sslmode=require
   DB_USERNAME=your_actual_username
   DB_PASSWORD=your_actual_password
   STOCK_API_KEY=HLNRH4Q2SC40WRHF
   ```

## Step 2: Start Docker Services

### Option A: Using Helper Script (Recommended)

Run the provided helper script:

```bash
./start-docker.sh
```

This will:
- Build the backend Spring Boot application
- Build the frontend React application  
- Start both services
- Connect to your Neon database

### Option B: Using Docker Compose (If Available)

If you have Docker Compose installed:

```bash
docker-compose up --build
```

## Step 3: Access Your Application

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health

## Useful Commands

```bash
# View backend logs
./logs.sh backend

# View frontend logs
./logs.sh frontend

# Stop all services
./stop-docker.sh

# Restart services
./stop-docker.sh && ./start-docker.sh
```

## Need Help?

See the full [DOCKER_SETUP.md](./DOCKER_SETUP.md) for detailed instructions and troubleshooting.

