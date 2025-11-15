-- V2__Seed_Data.sql
-- Seed data for testing and initial setup

-- Insert default admin partner
-- Password is 'admin123' (BCrypt hash with strength 12)
INSERT INTO partners (name, api_key, secret_hash, email, status, rate_limit_per_minute, rate_limit_per_hour, login_count, created_at, updated_at, description)
VALUES (
    'System Admin',
    'GW_ADMIN_DEFAULT_KEY_001',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIpSRelGyG',
    'admin@enterprise.com',
    'ACTIVE',
    1000,
    10000,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'Default system administrator account'
);

-- Insert admin role
INSERT INTO partner_roles (partner_id, role)
SELECT id, 'ADMIN' FROM partners WHERE api_key = 'GW_ADMIN_DEFAULT_KEY_001';

INSERT INTO partner_roles (partner_id, role)
SELECT id, 'USER' FROM partners WHERE api_key = 'GW_ADMIN_DEFAULT_KEY_001';

-- Insert test partner
-- Password is 'test123' (BCrypt hash with strength 12)
INSERT INTO partners (name, api_key, secret_hash, email, status, rate_limit_per_minute, rate_limit_per_hour, login_count, created_at, updated_at, description)
VALUES (
    'Test Partner',
    'GW_TEST_PARTNER_KEY_001',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'test@partner.com',
    'ACTIVE',
    60,
    1000,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'Test partner for development and testing'
);

-- Insert user role for test partner
INSERT INTO partner_roles (partner_id, role)
SELECT id, 'USER' FROM partners WHERE api_key = 'GW_TEST_PARTNER_KEY_001';

-- Note: In production, remove or change these default credentials
