# Barista Queue System

A full-stack application for managing a coffee shop queue system.

## Project Structure

- **backend**: Spring Boot application (Java 17)
- **frontend**: React application (TypeScript, Vite)
- **docker-compose.yml**: Orchestration for running the full stack locally

## Getting Started

### Prerequisites

- Docker and Docker Compose
- Java 17 (for local backend development)
- Node.js 18+ (for local frontend development)

### Running with Docker Compose

1. Build and start all services:
   ```bash
   docker-compose up --build
   ```
2. Access the application:
   - Frontend: [http://localhost:3000](http://localhost:3000)
   - Backend API: [http://localhost:8080](http://localhost:8080)

### Local Development

#### Backend
```bash
cd backend
mvn spring-boot:run
```

#### Frontend
```bash
cd frontend
npm install
npm run dev
```

## Features
- **Backend**: Spring Boot REST API with JPA/Hibernate.
- **Frontend**: React with TypeScript and Vite.
- **Database**: MySQL (in Docker) or H2 (local dev default).
