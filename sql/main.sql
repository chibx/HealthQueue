-- ENUMS for state management
CREATE TYPE appointment_status AS ENUM (
    'SCHEDULED', 'IN_QUEUE', 'IN_PROGRESS', 'COMPLETED', 'OVERSTAYED', 'CANCELLED'
);

-- 1. USERS (Patients/Students)
-- Uses UUID as requested. Includes coordinates for the Haversine distance calculation.
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    latitude DECIMAL(10, 8),  -- For distance calculations
    longitude DECIMAL(11, 8), -- For distance calculations
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. ORGANIZATIONS (Hospitals)
-- Isolated in its own table with a UUID.
CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    registration_code VARCHAR(100) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. BRANCHES (Hospital Branches/Clinics)
-- Uses BIGINT for your Java Snowflake class. 
CREATE TABLE branches (
    id BIGINT PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    address TEXT NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. DOCTORS / MEDICAL OFFICERS
-- Uses BIGINT. Tied to a specific branch.
CREATE TABLE doctors (
    id BIGINT PRIMARY KEY,
    branch_id BIGINT NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    specialty VARCHAR(150),
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. APPOINTMENTS & QUEUE MANAGEMENT
-- Highly transactional, perfect for Snowflake BIGINTs. 
CREATE TABLE appointments (
    id BIGINT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    branch_id BIGINT NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    doctor_id BIGINT NOT NULL REFERENCES doctors(id),
    scheduled_start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    scheduled_end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    actual_start_time TIMESTAMP WITH TIME ZONE,
    actual_end_time TIMESTAMP WITH TIME ZONE,
    status appointment_status DEFAULT 'SCHEDULED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. CLINIC VISITS (Historical Records)
-- To maintain a record of clinic visits as specified in the project requirements.
CREATE TABLE clinic_visits (
    id BIGINT PRIMARY KEY,
    appointment_id BIGINT UNIQUE NOT NULL REFERENCES appointments(id),
    user_id UUID NOT NULL REFERENCES users(id),
    doctor_id BIGINT NOT NULL REFERENCES doctors(id),
    visit_notes TEXT,
    prescription_details TEXT,
    recorded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- INDEXES for performance optimization
CREATE INDEX idx_users_location ON users(latitude, longitude);
CREATE INDEX idx_branches_location ON branches(latitude, longitude);
CREATE INDEX idx_appointments_user ON appointments(user_id);
CREATE INDEX idx_appointments_doctor_time ON appointments(doctor_id, scheduled_start_time);
CREATE INDEX idx_appointments_status ON appointments(status);