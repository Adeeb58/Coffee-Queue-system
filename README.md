# ☕ Barista Queue System

A full-stack implementation of a coffee shop queue management system, designed to handle high concurrency, real-time updates, and intelligent order assignment.

## 🚀 Overview

The **Barista Queue System** is a robust application that manages coffee orders, assigns them to baristas based on workload, and tracks metrics in real-time. It features a dual-interface design:
1.  **Customer Interface**: For placing orders.
2.  **Barista/Admin Dashboard**: For monitoring active orders, barista workload, and system performance.
3.  **Simulation & Testing**: A dedicated module to simulate heavy traffic (e.g., 100+ orders) and analyze system behavior under load.

## 🏗️ Architecture & Solution

### Tech Stack
-   **Backend**: Java Spring Boot (REST API, WebSocket, Scheduler)
-   **Frontend**: React.js (TypeScript, TailwindCSS, Recharts)
-   **Database**: MySQL 8.0 (Persistence)
-   **Containerization**: Docker & Docker Compose

### Key Features
-   **Intelligent Assignment**: Orders are automatically assigned to the barista with the lowest current workload (prep time).
-   **Priority Queueing**: Orders are prioritized based on wait time and customer loyalty status.
-   **Real-Time Updates**: WebSockets push status updates to the frontend instantly—no refreshing required.
-   **Simulated Load**: A Poisson-distribution based generator creates realistic order traffic to test system resilience.

## 🐳 Docker Deployment (Recommended)

The application is fully containerized. You can run the entire stack (Backend + Frontend + Database) on any device with Docker installed.

### Prerequisites
-   [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed.

### Steps to Run
1.  **Clone the Repository**
    ```bash
    git clone https://github.com/Adeeb58/Coffee-Queue-system.git
    cd Coffee-Queue-system
    ```

2.  **Run with Docker Compose**
    This command pulls the pre-built images from Docker Hub and starts the services.
    ```bash
    docker-compose up -d
    ```

3.  **Access the Application**
    -   **Frontend**: [http://localhost](http://localhost)
    -   **Backend API**: [http://localhost:8080](http://localhost:8080)

### Docker Hub Images
-   **Backend**: [`adeeb58/baristacoffee-backend`](https://hub.docker.com/r/adeeb58/baristacoffee-backend)
-   **Frontend**: [`adeeb58/baristacoffee-frontend`](https://hub.docker.com/r/adeeb58/baristacoffee-frontend)

## 🧪 Simulation Feature

The system includes a powerful simulation engine to visualize how the queue performs under pressure.

### How it Works
1.  Navigate to the **"Test Simulation"** tab in the dashboard.
2.  Click **"Generate 100 Orders"**.
3.  The system generates 100 orders with realistic timestamps (simulating the past hour).
4.  **Auto-Assignment**: Orders are instantly distributed among available baristas.
5.  **Real-Time Metrics**: View immediate data on:
    -   Average & Max Wait Times
    -   Orders Completed vs. Pending
    -   Barista efficiency and workload distribution

This allows stakeholders to optimize barista allocation and improve service efficiency.

## 🛠️ Local Development (Manual Setup)

If you prefer to run the code locally without Docker:

### Backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm start
```
