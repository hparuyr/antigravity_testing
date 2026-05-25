# Stock Dashboard

## Project Structure

```
TestGravity/
├── src/                    # Spring Boot backend
└── pom.xml                # Maven build file
```

## Local Development

```bash
mvn spring-boot:run
```

## Environment Variables

- `STOCK_API_KEY` - Alpha Vantage API key
- `DATABASE_URL` - PostgreSQL connection string
- `PORT` - Server port (default: 8080)

## Tech Stack

- **Backend**: Spring Boot, PostgreSQL, Alpha Vantage API
