# Distributed Inventory System - Run Guide

## Overview

This is a **Distributed Inventory System** built using **CQRS (Command Query Responsibility Segregation)** pattern with **Event Sourcing**. The system manages product inventory with separate command and query services that communicate via HTTP for real-time data synchronization.

## Architecture

- **Command Service** (`inventory.cmd`): Handles product creation and stock updates
- **Query Service** (`inventory.query`): Handles product queries and read operations
- **Core Framework** (`cqrs.core`): Shared CQRS infrastructure
- **Database**: H2 in-memory databases (separate for command and query services)
- **Communication**: HTTP-based synchronous updates between services

## Prerequisites

- **Docker** and **Docker Compose** installed
- **Java 17** (for local development)
- **Maven** (for local development)
- **Git** (for cloning the repository)

## Quick Start

### 1. Clone and Navigate to Project
```bash
git clone <repository-url>
cd distributed-inventory
```

### 2. Start the System
```bash
docker-compose up --build -d
```

### 3. Wait for Services to Start
The system will take approximately 30-60 seconds to fully start. You can monitor the logs:
```bash
docker-compose logs -f
```

### 4. Verify System is Running
```bash
# Check if services are up
docker-compose ps

# Test the API endpoints
curl -X GET http://localhost:8081/api/v1/products
```

## API Endpoints

### Command Service (Port 8080)
- **POST** `/api/v1/products` - Create a new product
- **PUT** `/api/v1/products/{id}/stock` - Update product stock

### Query Service (Port 8081)
- **GET** `/api/v1/products` - Get all products
- **GET** `/api/v1/products/{id}` - Get product by ID
- **POST** `/api/v1/products/test` - Create test product (for testing)
- **POST** `/api/v1/products/sync` - Internal sync endpoint

## Usage Examples

### Create a Product
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "category": "Electronics",
    "price": 1299.99,
    "storeId": "store1",
    "initialAmount": 50
  }'
```

### Get All Products
```bash
curl -X GET http://localhost:8081/api/v1/products
```

### Update Product Stock
```bash
curl -X PUT http://localhost:8080/api/v1/products/{product-id}/stock \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "purchase",
    "amount": 10
  }'
```

## Service Details

### Command Service (inventory.cmd)
- **Port**: 8080
- **Database**: H2 in-memory (command database)
- **Purpose**: Handles all write operations (create, update)
- **Features**: Event sourcing, command handling, HTTP sync to query service

### Query Service (inventory.query)
- **Port**: 8081
- **Database**: H2 in-memory (query database)
- **Purpose**: Handles all read operations (queries)
- **Features**: Read model updates, HTTP sync endpoint

### Core Framework (cqrs.core)
- **Purpose**: Shared CQRS infrastructure
- **Features**: Base classes, event handling, command/query patterns

## Database Access

### Command Service Database
- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:commanddb`
- **Username**: `sa`
- **Password**: (empty)

### Query Service Database
- **URL**: `http://localhost:8081/h2-console`
- **JDBC URL**: `jdbc:h2:mem:querydb`
- **Username**: `sa`
- **Password**: (empty)

## Development

### Local Development Setup
```bash
# Build all modules
mvn clean install

# Run command service
cd inventory.cmd
mvn spring-boot:run

# Run query service (in another terminal)
cd inventory.query
mvn spring-boot:run
```

### Project Structure
```
distributed-inventory/
├── inventory.cmd/          # Command service
│   ├── src/main/java/     # Java source code
│   ├── src/main/resources/ # Configuration files
│   └── pom.xml            # Maven configuration
├── inventory.query/        # Query service
│   ├── src/main/java/     # Java source code
│   ├── src/main/resources/ # Configuration files
│   └── pom.xml            # Maven configuration
├── cqrs.core/             # Core CQRS framework
│   ├── src/main/java/     # Core classes
│   └── pom.xml            # Maven configuration
├── docker-compose.yml     # Docker configuration
└── smoke-tests.sh         # Test script
```

## Testing

### Run Smoke Tests
```bash
./smoke-tests.sh
```

### Manual Testing
```bash
# Create a product
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Product", "description": "A test product", "category": "Electronics", "price": 99.99, "storeId": "store1", "initialAmount": 10}'

# Verify it appears in query service
curl -X GET http://localhost:8081/api/v1/products
```

## Troubleshooting

### Common Issues

1. **Services not starting**
   ```bash
   # Check Docker logs
   docker-compose logs
   
   # Restart services
   docker-compose down
   docker-compose up --build -d
   ```

2. **Port conflicts**
   - Ensure ports 8080 and 8081 are available
   - Check if other services are using these ports

3. **Database connection issues**
   - Wait for services to fully start (30-60 seconds)
   - Check H2 console URLs above

4. **API not responding**
   ```bash
   # Check service health
   curl -X GET http://localhost:8080/api/v1/products
   curl -X GET http://localhost:8081/api/v1/products
   ```

### Logs and Debugging
```bash
# View all logs
docker-compose logs

# View specific service logs
docker-compose logs inventory-cmd
docker-compose logs inventory-query

# Follow logs in real-time
docker-compose logs -f
```

## Configuration

### Environment Variables
- `SPRING_PROFILES_ACTIVE`: Active Spring profile
- `SERVER_PORT`: Service port (8080 for cmd, 8081 for query)
- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers

### Application Properties
- **Command Service**: `inventory.cmd/src/main/resources/application.yml`
- **Query Service**: `inventory.query/src/main/resources/application.yml`

## Production Considerations

### Security
- Change default database passwords
- Use environment variables for sensitive configuration
- Implement proper authentication and authorization

### Performance
- Configure connection pooling
- Implement caching strategies
- Monitor database performance

### Monitoring
- Add health check endpoints
- Implement metrics collection
- Set up logging aggregation

## Stopping the System

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clears all data)
docker-compose down -v
```

## Support

For issues or questions:
1. Check the logs: `docker-compose logs`
2. Verify all services are running: `docker-compose ps`
3. Test API endpoints manually
4. Check database connectivity via H2 console

## System Requirements

- **RAM**: Minimum 2GB, Recommended 4GB
- **Disk**: 1GB free space
- **CPU**: 2 cores minimum
- **Network**: Ports 8080, 8081, 9092, 2181 available
