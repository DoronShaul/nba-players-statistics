# NBA Player Statistics System

A scalable backend system for logging and analyzing NBA player statistics, built with Spring Boot and Java 8.

## Project Overview

This system provides APIs for recording NBA player game statistics and retrieving aggregated season averages for both players and teams. The application features:

- RESTful API for recording player statistics per game
- Endpoints for retrieving player and team season averages
- Horizontally scalable architecture with load balancing
- Redis caching for improved performance
- MySQL database for persistent storage

## Technologies Used

- **Backend**: Java 8, Spring Boot
- **Database**: MySQL 8.0
- **Caching**: Redis 7.0
- **Load Balancing**: Nginx
- **Containerization**: Docker & Docker Compose

## System Architecture

The system follows a microservices architecture with:
- Multiple application instances for horizontal scaling
- Redis caching layer for fast retrieval of aggregate statistics
- MySQL database for persistent storage
- Nginx as a load balancer to distribute traffic

## API Documentation

### Record Player Game Statistics

```POST /api/v1/games/{gameId}/stats```

Records statistics for multiple players in a single game.

**Request Body Example:**
```json

[
    {
    "playerId": 1,
    "gameId": 1,
    "points": 24,
    "rebounds": 10,
    "assists": 5,
    "steals": 2,
    "blocks": 1,
    "fouls": 3,
    "turnovers": 2,
    "minutesPlayed": 32.5
    },
    {
    "playerId": 2,
    "gameId": 1,
    "points": 18,
    "rebounds": 5,
    "assists": 8,
    "steals": 1,
    "blocks": 0,
    "fouls": 2,
    "turnovers": 3,
    "minutesPlayed": 28.0
    }
]
```

### Get Player Season Stats

```GET /api/v1/players/{playerId}/stats/averages?seasonId={seasonId}```

Retrieves the season averages for a specific player.

### Get Team Season Stats

```GET /api/v1/teams/{teamId}/stats/averages?seasonId={seasonId}```

Retrieves the season averages for all players on a specific team.

## Setup and Running Instructions

### Prerequisites
- Docker and Docker Compose installed
- Git

### Clone the Repository

```shell
git clone https://github.com/DoronShaul/nba-players-statistics.git
```
```shell
cd nba-players-statistics
```

### Running with Docker Compose
```shell
# Build and start all services
docker-compose up -d

# To stop all services
docker-compose down

# To view logs
docker-compose logs -f
```

The application will be accessible at:
- Main API endpoint: http://localhost:80/api/v1/

### Database Initialization
The database is automatically initialized with the schema from sql/init.sql. This creates the necessary tables and views for:
- Seasons, Teams, Players management
- Player-Team associations
- Games tracking
- Player statistics per game
- Aggregated season statistics views

## Performance Considerations

- The system uses Redis caching to minimize database load for frequently accessed statistics
- Horizontally scalable with multiple application instances
- Nginx load balancing for distributed request handling
- Database indexes optimized for common query patterns

## Testing the API

Here are some example cURL commands to test the API:

### Record Player Game Statistics
```shell
curl -X POST http://localhost/api/v1/games/1/stats \
-H "Content-Type: application/json" \
-d '[{"playerId": 1, gameId": 1, ""points": 24, "rebounds": 10, "assists": 5, "steals": 2, "blocks": 1, "fouls": 3, "turnovers": 2, "minutesPlayed": 32.5}]'
```

### Get Player Season Stats
```shell
curl -X GET http://localhost/api/v1/players/1/stats/averages?seasonId=1
```

### Get Team Season Stats
```shell
curl -X GET http://localhost/api/v1/teams/1/stats/averages?seasonId=1
```