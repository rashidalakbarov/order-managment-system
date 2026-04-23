# Docker Database Setup

This project uses PostgreSQL for the main application database.

## Start database

```bash
docker compose up -d
```

## Stop database

```bash
docker compose down
```

## Stop database and remove volume

```bash
docker compose down -v
```

## Connection settings

- Host: `localhost`
- Port: `5432`
- Database: `order_db`
- Username: `postgres`
- Password: `password`

The Spring datasource reads these values from environment variables if provided:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

You can copy `.env.example` to `.env` and adjust values if needed.
