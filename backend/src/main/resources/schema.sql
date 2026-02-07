-- Create database (already handled by createDatabaseIfNotExist in connection string)
-- Drinks Menu Table
CREATE TABLE IF NOT EXISTS drinks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    prep_time INT NOT NULL COMMENT 'Preparation time in minutes',
    frequency DECIMAL(5, 2) NOT NULL COMMENT 'Frequency percentage',
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
-- Baristas Table
CREATE TABLE IF NOT EXISTS baristas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    status ENUM('AVAILABLE', 'BUSY', 'OFFLINE') DEFAULT 'AVAILABLE',
    current_workload INT DEFAULT 0 COMMENT 'Current workload in minutes',
    total_orders_served INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
-- Customers Table
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    phone VARCHAR(15),
    loyalty_status ENUM('NEW', 'REGULAR', 'GOLD') DEFAULT 'NEW',
    total_visits INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- Orders Table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(20) UNIQUE NOT NULL,
    customer_id BIGINT,
    drink_id BIGINT NOT NULL,
    barista_id BIGINT,
    quantity INT DEFAULT 1,
    status ENUM(
        'PENDING',
        'IN_PROGRESS',
        'COMPLETED',
        'CANCELLED'
    ) DEFAULT 'PENDING',
    priority_score DECIMAL(5, 2) DEFAULT 0,
    order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_time TIMESTAMP NULL,
    completion_time TIMESTAMP NULL,
    wait_time_minutes INT,
    skipped_count INT DEFAULT 0 COMMENT 'How many later orders were served first',
    emergency_flag BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (drink_id) REFERENCES drinks(id),
    FOREIGN KEY (barista_id) REFERENCES baristas(id)
);
-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_priority ON orders(priority_score DESC);
CREATE INDEX IF NOT EXISTS idx_orders_time ON orders(order_time);
CREATE INDEX IF NOT EXISTS idx_baristas_status ON baristas(status);
-- Insert sample drinks (only if table is empty)
INSERT IGNORE INTO drinks (id, name, prep_time, frequency, price)
VALUES (1, 'Cold Brew', 1, 25.00, 120.00),
    (2, 'Espresso', 2, 20.00, 150.00),
    (3, 'Americano', 2, 15.00, 140.00),
    (4, 'Cappuccino', 4, 20.00, 180.00),
    (5, 'Latte', 4, 12.00, 200.00),
    (6, 'Specialty (Mocha)', 6, 8.00, 250.00);
-- Insert sample baristas (only if table is empty)
INSERT IGNORE INTO baristas (id, name, status)
VALUES (1, 'Barista A', 'AVAILABLE'),
    (2, 'Barista B', 'AVAILABLE'),
    (3, 'Barista C', 'AVAILABLE');
-- Insert sample customers (only if table is empty)
INSERT IGNORE INTO customers (id, name, phone, loyalty_status, total_visits)
VALUES (1, 'John Doe', '9876543210', 'REGULAR', 45),
    (2, 'Jane Smith', '9876543211', 'GOLD', 120),
    (3, 'Alice Johnson', '9876543212', 'NEW', 1);