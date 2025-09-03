-- PostgreSQL initialization script for dispatch database

-- Create rides table
CREATE TABLE rides (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rider_id VARCHAR(255) NOT NULL,
    driver_id VARCHAR(255),
    pickup_lat DECIMAL(10, 8) NOT NULL,
    pickup_lng DECIMAL(11, 8) NOT NULL,
    destination_lat DECIMAL(10, 8),
    destination_lng DECIMAL(11, 8),
    status VARCHAR(50) NOT NULL DEFAULT 'REQUESTED',
    fare_amount DECIMAL(10, 2),
    estimated_duration_minutes INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    CONSTRAINT valid_status CHECK (status IN ('REQUESTED', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

-- Create drivers table
CREATE TABLE drivers (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    license_plate VARCHAR(20),
    current_lat DECIMAL(10, 8),
    current_lng DECIMAL(11, 8),
    status VARCHAR(50) NOT NULL DEFAULT 'OFFLINE',
    last_location_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_driver_status CHECK (status IN ('OFFLINE', 'AVAILABLE', 'BUSY', 'EN_ROUTE'))
);

-- Create riders table
CREATE TABLE riders (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_rides_status ON rides(status);
CREATE INDEX idx_rides_driver_id ON rides(driver_id);
CREATE INDEX idx_rides_created_at ON rides(created_at);
CREATE INDEX idx_drivers_status ON drivers(status);
CREATE INDEX idx_drivers_location ON drivers(current_lat, current_lng);
CREATE INDEX idx_drivers_last_update ON drivers(last_location_update);

-- Insert sample data
INSERT INTO riders (id, name, phone, email) VALUES 
('rider_001', 'John Doe', '+1234567890', 'john@example.com'),
('rider_002', 'Jane Smith', '+1234567891', 'jane@example.com'),
('rider_003', 'Bob Johnson', '+1234567892', 'bob@example.com');

INSERT INTO drivers (id, name, phone, license_plate, current_lat, current_lng, status) VALUES 
('driver_001', 'Mike Wilson', '+1234567893', 'ABC123', 40.7589, -73.9851, 'AVAILABLE'),
('driver_002', 'Sarah Davis', '+1234567894', 'XYZ456', 40.7614, -73.9776, 'AVAILABLE'),
('driver_003', 'Tom Brown', '+1234567895', 'DEF789', 40.7505, -73.9934, 'AVAILABLE'),
('driver_004', 'Lisa Garcia', '+1234567896', 'GHI012', 40.7549, -73.9840, 'AVAILABLE'),
('driver_005', 'David Lee', '+1234567897', 'JKL345', 40.7580, -73.9855, 'AVAILABLE');
