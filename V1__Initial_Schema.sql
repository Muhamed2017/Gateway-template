-- V1__Initial_Schema.sql
-- Initial database schema for API Gateway

-- Partners table
CREATE TABLE partners (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    api_key VARCHAR(100) NOT NULL UNIQUE,
    secret_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    rate_limit_per_minute INTEGER,
    rate_limit_per_hour INTEGER,
    last_login TIMESTAMP,
    login_count BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    description VARCHAR(500),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING'))
);

-- Partner roles table
CREATE TABLE partner_roles (
    partner_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (partner_id, role),
    FOREIGN KEY (partner_id) REFERENCES partners(id) ON DELETE CASCADE
);

-- Partner allowed IPs table
CREATE TABLE partner_allowed_ips (
    partner_id BIGINT NOT NULL,
    ip_address VARCHAR(50) NOT NULL,
    PRIMARY KEY (partner_id, ip_address),
    FOREIGN KEY (partner_id) REFERENCES partners(id) ON DELETE CASCADE
);

-- Audit logs table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    partner_id BIGINT,
    partner_name VARCHAR(100),
    request_id VARCHAR(100) UNIQUE,
    method VARCHAR(10) NOT NULL,
    endpoint VARCHAR(500) NOT NULL,
    query_params TEXT,
    request_body TEXT,
    response_body TEXT,
    http_status INTEGER,
    client_ip VARCHAR(50),
    user_agent VARCHAR(500),
    duration_ms BIGINT,
    error_message TEXT,
    stack_trace TEXT,
    timestamp TIMESTAMP NOT NULL,
    correlation_id VARCHAR(100),
    downstream_service VARCHAR(100),
    cache_hit BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (partner_id) REFERENCES partners(id) ON DELETE SET NULL
);

-- Indexes for better query performance
CREATE INDEX idx_partner_api_key ON partners(api_key);
CREATE INDEX idx_partner_status ON partners(status);
CREATE INDEX idx_partner_email ON partners(email);

CREATE INDEX idx_audit_partner ON audit_logs(partner_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_status ON audit_logs(http_status);
CREATE INDEX idx_audit_endpoint ON audit_logs(endpoint);
CREATE INDEX idx_audit_request_id ON audit_logs(request_id);

-- Comments for documentation
COMMENT ON TABLE partners IS 'API consumer partners with authentication and authorization details';
COMMENT ON TABLE audit_logs IS 'Comprehensive audit trail of all API requests';
COMMENT ON COLUMN partners.api_key IS 'Unique API key for partner authentication';
COMMENT ON COLUMN partners.secret_hash IS 'BCrypt hashed secret for authentication';
COMMENT ON COLUMN partners.rate_limit_per_minute IS 'Maximum requests allowed per minute';
COMMENT ON COLUMN audit_logs.duration_ms IS 'Request processing time in milliseconds';
