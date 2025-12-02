#!/bin/bash

# View logs for Stock Dashboard services

if [ "$1" == "backend" ]; then
    echo "📋 Backend logs (Ctrl+C to exit):"
    docker logs -f stock-backend
elif [ "$1" == "frontend" ]; then
    echo "📋 Frontend logs (Ctrl+C to exit):"
    docker logs -f stock-frontend
else
    echo "Usage: ./logs.sh [backend|frontend]"
    echo ""
    echo "Examples:"
    echo "  ./logs.sh backend   - View backend logs"
    echo "  ./logs.sh frontend  - View frontend logs"
fi
