# Docker Setup Guide - Local Development with Neon Database

This guide will help you run your Stock Dashboard application locally using Docker, connected to your Neon PostgreSQL database.

## Prerequisites

- Docker and Docker Compose installed on your machine
- Neon database account and connection details
- Git repository cloned locally

## Step 1: Get Your Neon Database Credentials

1. Log in to your [Neon Console](https://console.neon.tech)
2. Select your project
3. Go to the **Dashboard** or **Connection Details** section
4. Copy the following information:
   - **Connection String** (looks like: `postgresql://username:password@ep-xxxxx.region.aws.neon.tech/dbname?sslmode=require`)
   - **Username**
   - **Password**
   - **Database Name**

## Step 2: Configure Environment Variables

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

2. Edit the `.env` file and replace the placeholder values with your actual Neon credentials:
   ```env
   DATABASE_URL=postgresql://your_username:your_password@ep-xxxxx.region.aws.neon.tech/your_dbname?sslmode=require
   DB_USERNAME=your_neon_username
   DB_PASSWORD=your_neon_password
   STOCK_API_KEY=HLNRH4Q2SC40WRHF
   ```

   > **Note**: The `DATABASE_URL` should be the complete connection string from Neon, including `?sslmode=require` at the end.

## Step 3: Build and Run with Docker Compose

1. Build and start all services:
   ```bash
   docker-compose up --build
   ```

   This will:
   - Build the backend Spring Boot application
   - Build the frontend React application
   - Start both services
   - Connect to your Neon database

2. Wait for the services to start. You should see logs indicating:
   - Backend: `Started StockDbApplication in X seconds`
   - Frontend: `VITE vX.X.X ready in X ms`

## Step 4: Access Your Application

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health

## Docker Commands Reference

### Start Services
```bash
# Start in foreground (see logs)
docker-compose up

# Start in background (detached mode)
docker-compose up -d

# Rebuild and start
docker-compose up --build
```

### Stop Services
```bash
# Stop services (keeps containers)
docker-compose stop

# Stop and remove containers
docker-compose down

# Stop and remove containers, volumes, and images
docker-compose down -v --rmi all
```

### View Logs
```bash
# View all logs
docker-compose logs

# View specific service logs
docker-compose logs backend
docker-compose logs frontend

# Follow logs in real-time
docker-compose logs -f

# View last 100 lines
docker-compose logs --tail=100
```

### Restart Services
```bash
# Restart all services
docker-compose restart

# Restart specific service
docker-compose restart backend
```

## Development Workflow

### Hot Reload

The frontend is configured with hot module replacement (HMR):
- Edit files in `frontend/src/`
- Changes will automatically reflect in the browser
- No need to rebuild the container

### Making Backend Changes

If you modify backend code:
```bash
# Rebuild and restart backend
docker-compose up --build backend
```

## Troubleshooting

### Database Connection Issues

**Error**: `Connection refused` or `Unable to connect to database`

**Solutions**:
1. Verify your `.env` file has correct Neon credentials
2. Check that the connection string includes `?sslmode=require`
3. Ensure your Neon database is active (not suspended)
4. Check Neon dashboard for connection limits

### Port Already in Use

**Error**: `Bind for 0.0.0.0:8080 failed: port is already allocated`

**Solutions**:
1. Stop any services using ports 8080 or 5173:
   ```bash
   # Find process using port
   lsof -i :8080
   lsof -i :5173
   
   # Kill the process
   kill -9 <PID>
   ```

2. Or change ports in `docker-compose.yml`:
   ```yaml
   ports:
     - "8081:8080"  # Use 8081 instead of 8080
   ```

### Backend Won't Start

**Error**: Build failures or startup errors

**Solutions**:
1. Check backend logs:
   ```bash
   docker-compose logs backend
   ```

2. Verify Java version compatibility (should be Java 17)

3. Clean and rebuild:
   ```bash
   docker-compose down
   docker-compose up --build
   ```

### Frontend Won't Start

**Error**: `npm install` failures or build errors

**Solutions**:
1. Remove node_modules and rebuild:
   ```bash
   cd frontend
   rm -rf node_modules package-lock.json
   cd ..
   docker-compose up --build frontend
   ```

2. Check frontend logs:
   ```bash
   docker-compose logs frontend
   ```

### Database Tables Not Created

**Issue**: Tables don't exist in Neon database

**Solutions**:
1. Check that `spring.jpa.hibernate.ddl-auto=update` is set in `application.properties`
2. Verify database connection is successful in backend logs
3. Check Neon dashboard to see if tables were created
4. You can manually run the `schema.sql` file if needed

## Environment Variables Reference

| Variable | Description | Example |
|----------|-------------|---------|
| `DATABASE_URL` | Full Neon connection string | `postgresql://user:pass@ep-xxx.region.aws.neon.tech/db?sslmode=require` |
| `DB_USERNAME` | Neon database username | `your_username` |
| `DB_PASSWORD` | Neon database password | `your_password` |
| `STOCK_API_KEY` | Alpha Vantage API key | `HLNRH4Q2SC40WRHF` |

## Next Steps

1. ✅ Verify both services are running
2. ✅ Check database connection in backend logs
3. ✅ Access the frontend at http://localhost:5173
4. ✅ Test API endpoints
5. ✅ Verify data is being stored in Neon database

## Additional Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Neon Documentation](https://neon.tech/docs)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)

## Support

If you encounter issues not covered in this guide:
1. Check the logs: `docker-compose logs`
2. Verify your Neon database is active
3. Ensure all environment variables are set correctly
4. Try rebuilding: `docker-compose up --build`
