-- =====================================================
-- V1__Create_base_schema.sql
-- Initial database schema for NPAxis Backend
-- =====================================================
-- This migration creates all foundational tables:
-- - Roles (authentication/authorization)
-- - Users (core user management)
-- - Credentials (preceptor credentials)
-- - Specialties (medical specialties)
-- Join tables and all relationships
-- =====================================================

-- =====================================================
-- 1. ROLES TABLE
-- =====================================================
-- Stores available roles for role-based access control
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
);

-- Create index on role name for faster lookups
CREATE INDEX idx_roles_name ON roles(name);

-- =====================================================
-- 2. USERS TABLE
-- =====================================================
-- Stores all user accounts (preceptors, students, admins)
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    account_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    role_id BIGINT REFERENCES roles(id)
);

-- Create indexes for common queries
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_deleted ON users(deleted);
CREATE INDEX idx_user_role_id ON users(role_id);

-- =====================================================
-- 3. USER_AUTHORITIES TABLE
-- =====================================================
-- Many-to-many mapping between users and authorities
CREATE TABLE IF NOT EXISTS user_authorities (
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    authority VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, authority)
);

-- =====================================================
-- 4. CREDENTIALS TABLE
-- =====================================================
-- Stores medical credentials (MBBS, MD, PhD)
-- Supports both system-predefined and user-created credentials
CREATE TABLE IF NOT EXISTS credentials (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    name_normalized VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_predefined BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
);

-- Create index for case-insensitive credential lookups
CREATE INDEX idx_credential_name_normalized ON credentials(name_normalized);
CREATE INDEX idx_credential_deleted ON credentials(deleted);

-- =====================================================
-- 5. SPECIALTIES TABLE
-- =====================================================
-- Stores medical specialties (Cardiology, Pediatrics, etc.)
-- Supports both system-predefined and user-created specialties
CREATE TABLE IF NOT EXISTS specialties (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    name_normalized VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_predefined BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
);

-- Create index for case-insensitive specialty lookups
CREATE INDEX idx_specialty_name_normalized ON specialties(name_normalized);
CREATE INDEX idx_specialty_deleted ON specialties(deleted);

-- =====================================================
-- 6. PRECEPTORS TABLE
-- =====================================================
-- Stores preceptor-specific information
CREATE TABLE IF NOT EXISTS preceptors (
    user_id BIGINT PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    location VARCHAR(150),
    setting VARCHAR(100),
    honorarium VARCHAR(100),
    license_number VARCHAR(50),
    license_state VARCHAR(50),
    license_file_url VARCHAR(500),
    requirements TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_premium BOOLEAN NOT NULL DEFAULT FALSE,
    verification_status VARCHAR(50),
    verification_submitted_at TIMESTAMP,
    verification_reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
);

-- Create indexes for preceptor queries
CREATE INDEX idx_preceptor_verified ON preceptors(is_verified);
CREATE INDEX idx_preceptor_premium ON preceptors(is_premium);
CREATE INDEX idx_preceptor_deleted ON preceptors(deleted);

-- =====================================================
-- 7. PRECEPTOR_CREDENTIALS (Join Table)
-- =====================================================
-- Many-to-many relationship between preceptors and credentials
CREATE TABLE IF NOT EXISTS preceptor_credentials (
    preceptor_id BIGINT NOT NULL REFERENCES preceptors(user_id) ON DELETE CASCADE,
    credential_id BIGINT NOT NULL REFERENCES credentials(id) ON DELETE CASCADE,
    PRIMARY KEY (preceptor_id, credential_id)
);

-- Create indexes for join table queries
CREATE INDEX idx_preceptor_credentials_credential ON preceptor_credentials(credential_id);

-- =====================================================
-- 8. PRECEPTOR_SPECIALTIES (Join Table)
-- =====================================================
-- Many-to-many relationship between preceptors and specialties
CREATE TABLE IF NOT EXISTS preceptor_specialties (
    preceptor_id BIGINT NOT NULL REFERENCES preceptors(user_id) ON DELETE CASCADE,
    specialty_id BIGINT NOT NULL REFERENCES specialties(id) ON DELETE CASCADE,
    PRIMARY KEY (preceptor_id, specialty_id)
);

-- Create indexes for join table queries
CREATE INDEX idx_preceptor_specialties_specialty ON preceptor_specialties(specialty_id);

-- =====================================================
-- 9. PRECEPTOR_AVAILABLE_DAYS (Element Collection)
-- =====================================================
-- Stores available days for preceptors
CREATE TABLE IF NOT EXISTS preceptor_available_days (
    preceptor_id BIGINT NOT NULL REFERENCES preceptors(user_id) ON DELETE CASCADE,
    day VARCHAR(20) NOT NULL,
    PRIMARY KEY (preceptor_id, day)
);

-- =====================================================
-- 10. SUBSCRIPTION_PLANS TABLE
-- =====================================================
-- Stores subscription plan definitions (FREE, PRO, ELITE)
CREATE TABLE IF NOT EXISTS subscription_plans (
    subscription_plan_id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    feature_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
);

-- Create indexes for subscription plan queries
CREATE INDEX idx_subscription_plans_code ON subscription_plans(code);
CREATE INDEX idx_subscription_plans_active ON subscription_plans(active);
CREATE INDEX idx_subscription_plans_deleted ON subscription_plans(deleted);

-- =====================================================
-- 11. SUBSCRIPTION_PRICES TABLE
-- =====================================================
-- Stores pricing information for subscription plans
-- Unique constraint ensures one price per (plan, billing_interval, currency) combination
CREATE TABLE IF NOT EXISTS subscription_prices (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES subscription_plans(subscription_plan_id),
    billing_interval VARCHAR(20) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'usd',
    amount_in_minor_units BIGINT NOT NULL,
    stripe_product_id VARCHAR(120) NOT NULL,
    stripe_price_id VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    UNIQUE(plan_id, billing_interval, currency)
);

-- Create indexes for subscription price queries
CREATE INDEX idx_subscription_prices_plan ON subscription_prices(plan_id);
CREATE INDEX idx_subscription_prices_active ON subscription_prices(active);
CREATE INDEX idx_subscription_prices_billing_interval ON subscription_prices(billing_interval);
CREATE INDEX idx_subscription_prices_deleted ON subscription_prices(deleted);

-- =====================================================
-- 12. PRECEPTOR_SUBSCRIPTIONS TABLE
-- =====================================================
-- Stores subscription information for each preceptor
CREATE TABLE IF NOT EXISTS preceptor_subscriptions (
    preceptor_subscription_id BIGSERIAL PRIMARY KEY,
    preceptor_id BIGINT NOT NULL UNIQUE REFERENCES preceptors(user_id),
    plan_id BIGINT NOT NULL REFERENCES subscription_plans(subscription_plan_id),
    price_id BIGINT NOT NULL REFERENCES subscription_prices(id),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    stripe_customer_id VARCHAR(120) NOT NULL,
    stripe_subscription_id VARCHAR(120) NOT NULL UNIQUE,
    current_period_start TIMESTAMP,
    current_period_end TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
);

-- Create indexes for subscription queries
CREATE INDEX idx_preceptor_subscriptions_preceptor ON preceptor_subscriptions(preceptor_id);
CREATE INDEX idx_preceptor_subscriptions_stripe ON preceptor_subscriptions(stripe_subscription_id);
CREATE INDEX idx_preceptor_subscriptions_status ON preceptor_subscriptions(status);
CREATE INDEX idx_preceptor_subscriptions_deleted ON preceptor_subscriptions(deleted);

-- =====================================================
-- 13. TOKENS TABLE
-- =====================================================
-- Stores OTP tokens for email verification
CREATE TABLE IF NOT EXISTS tokens (
    token_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    hashed_otp VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMP
);

-- Create indexes for token queries
CREATE INDEX idx_tokens_email ON tokens(email);
CREATE INDEX idx_tokens_expires ON tokens(expires_at);

-- =====================================================
-- 14. INVOICES TABLE (for future use)
-- =====================================================
-- Stores invoice records
CREATE TABLE IF NOT EXISTS invoices (
    invoice_id BIGSERIAL PRIMARY KEY,
    preceptor_subscription_id BIGINT NOT NULL REFERENCES preceptor_subscriptions(preceptor_subscription_id),
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    amount_in_minor_units BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'usd',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    invoice_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP,
    paid_date TIMESTAMP,
    stripe_invoice_id VARCHAR(120),
    pdf_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
);

-- Create indexes for invoice queries
CREATE INDEX idx_invoices_subscription ON invoices(preceptor_subscription_id);
CREATE INDEX idx_invoices_number ON invoices(invoice_number);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_stripe ON invoices(stripe_invoice_id);
CREATE INDEX idx_invoices_deleted ON invoices(deleted);

-- =====================================================
-- Migration completed successfully
-- =====================================================

