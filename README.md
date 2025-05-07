# Spring Boot and Node.js Backend Project

This project demonstrates a backend application using Spring Boot (Java) and Node.js, leveraging the strengths of both technologies.

## Architecture

The application consists of two main components:

1. **Spring Boot Application**: Handles core business logic, database operations, security, and main API endpoints.
2. **Node.js Application**: Manages real-time features using WebSockets and handles lightweight microservices.

Both applications communicate with each other through RESTful API calls and share the same MongoDB database.

## Prerequisites

- Java 17 or higher
- Node.js 16 or higher
- MongoDB

## Configuration

### Spring Boot Application

Configure the Spring Boot application by editing the `src/main/resources/application.yml` file:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/spring_node_db

jwt:
  secret: your-secret-key
  expiration: 86400000 # 24 hours
```

### Node.js Application

Create a `.env` file in the root directory:

```
PORT=3000
MONGODB_URI=mongodb://localhost:27017/spring_node_db
JWT_SECRET=your-secret-key
```

## Running the Application

### Spring Boot

```bash
./mvnw spring-boot:run
```

### Node.js

```bash
npm install
npm start
```

## API Documentation

### Spring Boot API

Once the Spring Boot application is running, access the API documentation at:

```
http://localhost:8080/api/swagger-ui.html
```

### Available Endpoints

#### Authentication

- `POST /api/auth/signup` - Register a new user
- `POST /api/auth/signin` - Authenticate a user

#### Node.js Service

- `GET /api/notifications` - Get all notifications for the current user
- `POST /api/notifications` - Create a new notification
- `GET /api/events` - Get all events
- `POST /api/events` - Create a new event

## Security

The application implements JWT-based authentication. To access protected endpoints, include the JWT token in the Authorization header:

```
Authorization: Bearer your-jwt-token
```

## Integration Points

- Spring Boot communicates with Node.js services using `NodeServiceClient`
- Both applications share the same MongoDB database
- Real-time updates are handled via Socket.IO in the Node.js application

## Project Structure

- `src/main/java` - Spring Boot application code
- `src/node` - Node.js application code