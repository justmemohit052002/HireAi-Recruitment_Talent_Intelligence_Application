-- ==============================================================================
-- Recruitment & Talent Intelligence Platform - V2 Job & Skill Schema
-- ==============================================================================

-- 1. Job Table
CREATE TABLE IF NOT EXISTS job (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    mode VARCHAR(50) NOT NULL DEFAULT 'SMART', -- SMART or CUSTOM
    location VARCHAR(255),
    experience_min INT DEFAULT 0,
    experience_max INT,
    interview_type VARCHAR(100) DEFAULT 'Core Knowledge Evaluation',
    interview_duration_minutes INT DEFAULT 30,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN', -- DRAFT, OPEN, ON_HOLD, CLOSED
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_job_org_status ON job(organization_id, status);
CREATE INDEX IF NOT EXISTS idx_job_created_at ON job(created_at DESC);

-- 2. Master Skill Catalog Table
CREATE TABLE IF NOT EXISTS skill (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    normalized_name VARCHAR(150) NOT NULL UNIQUE,
    category VARCHAR(100) DEFAULT 'General',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_skill_normalized_name ON skill(normalized_name);

-- 3. Job <-> Skill Mapping Table
CREATE TABLE IF NOT EXISTS job_skill (
    job_id UUID NOT NULL REFERENCES job(id) ON DELETE CASCADE,
    skill_id UUID NOT NULL REFERENCES skill(id) ON DELETE CASCADE,
    required BOOLEAN NOT NULL DEFAULT TRUE,
    weightage INT DEFAULT 0,
    PRIMARY KEY (job_id, skill_id)
);

-- 4. Custom Focus Areas (for Custom Mode / evaluation criteria)
CREATE TABLE IF NOT EXISTS job_focus_area (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES job(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    weightage INT NOT NULL DEFAULT 25,
    difficulty_level VARCHAR(50) NOT NULL DEFAULT 'MEDIUM', -- EASY, MEDIUM, HARD
    topics TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_focus_area_job_id ON job_focus_area(job_id);

-- 5. Job Sample / Suggested Interview Questions
CREATE TABLE IF NOT EXISTS job_sample_question (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES job(id) ON DELETE CASCADE,
    focus_area_id UUID REFERENCES job_focus_area(id) ON DELETE SET NULL,
    category VARCHAR(150),
    question_text TEXT NOT NULL,
    difficulty VARCHAR(50) DEFAULT 'MEDIUM',
    sort_order INT DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sample_question_job_id ON job_sample_question(job_id);
