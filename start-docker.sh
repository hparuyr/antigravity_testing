#!/bin/bash

# Docker Setup Script for Stock Dashboard
# This script helps you run the application using Docker

set -e

echo "🚀 Stock Dashboard - Docker Setup"
echo "=================================="
echo ""

# Check if .env file exists and has been configured
if [ ! -f .env ]; then
    echo "❌ Error: .env file not found!"
    echo "Please copy .env.example to .env and fill in your Neon credentials."
    echo ""
    echo "Run: cp .env.example .env"
    echo "Then edit .env with your Neon database connection details."
    exit 1
fi

# Check if .env has been configured (look for placeholder values)
if grep -q "your_username" .env || grep -q "your_password" .env; then
    echo "⚠️  Warning: .env file contains placeholder values!"
    echo "Please update .env with your actual Neon database credentials."
    echo ""
    read -p "Have you updated the .env file? (y/n) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Please update .env and run this script again."
        exit 1
    fi
fi

# Load environment variables
export $(cat .env | grep -v '^#' | xargs)

echo "✅ Environment variables loaded"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker is not running!"
    echo "Please start Docker Desktop and try again."
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Build and start services
echo "🔨 Building Docker images..."
echo ""

# Build backend
echo "📦 Building backend..."
docker build -t stock-backend:latest .

# Build frontend
echo "📦 Building frontend..."
docker build -f frontend/Dockerfile.dev -t stock-frontend:latest frontend/

echo ""
echo "✅ Images built successfully"
echo ""

# Create network if it doesn't exist
docker network inspect stock-network >/dev/null 2>&1 || docker network create stock-network

echo "🚀 Starting services..."
echo ""

# Stop and remove existing containers if they exist
docker stop stock-backend 2>/dev/null || true
docker rm stock-backend 2>/dev/null || true
docker stop stock-frontend 2>/dev/null || true
docker rm stock-frontend 2>/dev/null || true

# Start backend
echo "🔧 Starting backend on port 8080..."
docker run -d \
  --name stock-backend \
  --network stock-network \
  -p 8080:8080 \
  -e DATABASE_URL="$DATABASE_URL" \
  -e DB_USERNAME="$DB_USERNAME" \
  -e DB_PASSWORD="$DB_PASSWORD" \
  -e STOCK_API_KEY="$STOCK_API_KEY" \
  -e CORS_ALLOWED_ORIGINS="http://localhost:5173" \
  stock-backend:latest

# Start frontend
echo "🎨 Starting frontend on port 5173..."
docker run -d \
  --name stock-frontend \
  --network stock-network \
  -p 5173:5173 \
  -e VITE_API_URL="http://localhost:8080" \
  -v "$(pwd)/frontend/src:/app/src" \
  -v "$(pwd)/frontend/public:/app/public" \
  stock-frontend:latest

echo ""
echo "✅ Services started successfully!"
echo ""
echo "📍 Access your application:"
echo "   Frontend: http://localhost:5173"
echo "   Backend:  http://localhost:8080"
echo "   Health:   http://localhost:8080/actuator/health"
echo ""
echo "📊 View logs:"
echo "   Backend:  docker logs -f stock-backend"
echo "   Frontend: docker logs -f stock-frontend"
echo ""
echo "🛑 Stop services:"
echo "   ./stop-docker.sh"
echo ""
