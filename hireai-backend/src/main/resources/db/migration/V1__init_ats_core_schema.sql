-- ==============================================================================
-- Recruitment & Talent Intelligence Platform - V1 Auth & Organization Schema
-- ==============================================================================

-- Enable pgcrypto for UUID generation if needed (gen_random_uuid is native in Postgres 13+)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. Organization Table
CREATE TABLE IF NOT EXISTS organization (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Role Table
CREATE TABLE IF NOT EXISTS role (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

-- 3. Users Table (using 'users' to avoid SQL reserved keyword conflict)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID REFERENCES organization(id) ON DELETE SET NULL,
    full_name VARCHAR(255) NOT NULL,
    work_email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    account_status VARCHAR(50) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_work_email ON users(work_email);
CREATE INDEX IF NOT EXISTS idx_users_organization_id ON users(organization_id);

-- 4. User Role Mapping Table
CREATE TABLE IF NOT EXISTS user_role (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id VARCHAR(50) NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 5. Email Verification PIN Table
-- Login identifier: work_email. Verification: short-lived 6-digit PIN, hashed at rest, expiry + attempt limit.
CREATE TABLE IF NOT EXISTS email_verification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    pin_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    consumed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_email_verification_user_id ON email_verification(user_id);

-- Seed System Roles
INSERT INTO role (id, name, description) VALUES
    ('ROLE_SUPER_ADMIN', 'Super Admin', 'Platform-wide administrator with full system permissions'),
    ('ROLE_ORG_ADMIN', 'Organization Admin', 'Tenant owner / HR Director managing organization settings, billing, and team'),
    ('ROLE_RECRUITER', 'Recruiter', 'Talent acquisition manager handling jobs, screening resumes, and scheduling'),
    ('ROLE_HIRING_MANAGER', 'Hiring Manager', 'Department manager reviewing candidates and approving hiring decisions'),
    ('ROLE_INTERVIEWER', 'Interviewer', 'Panel interviewer conducting interviews and submitting scorecards'),
    ('ROLE_CANDIDATE', 'Candidate', 'Job applicant accessing candidate portal and assessments')
ON CONFLICT (id) DO NOTHING;
