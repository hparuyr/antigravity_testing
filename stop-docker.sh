#!/bin/bash

# Stop Docker containers for Stock Dashboard

echo "🛑 Stopping Stock Dashboard services..."
echo ""

# Stop containers
docker stop stock-backend stock-frontend 2>/dev/null || true

# Remove containers
docker rm stock-backend stock-frontend 2>/dev/null || true

echo "✅ Services stopped and removed"
echo ""
echo "To start again, run: ./start-docker.sh"
