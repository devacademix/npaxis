-- =====================================================
-- V1__Create_core_schema.sql
-- Core schema with roles, users, credentials, and specialties
-- =====================================================

-- ==================== ROLES TABLE ====================
CREATE TABLE IF NOT EXISTS roles
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    name
    VARCHAR
(
    50
) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT ck_role_name_not_empty CHECK
(
    TRIM
(
    name
) <> '')
    );

CREATE INDEX IF NOT EXISTS idx_roles_name ON roles(name);
CREATE INDEX IF NOT EXISTS idx_roles_deleted ON roles(deleted) WHERE deleted = FALSE;

-- ==================== USERS TABLE ====================
CREATE TABLE IF NOT EXISTS users
(
    user_id
    BIGSERIAL
    PRIMARY
    KEY,
    email
    VARCHAR
(
    255
) NOT NULL UNIQUE,
    password VARCHAR
(
    255
) NOT NULL,
    display_name VARCHAR
(
    255
) NOT NULL,
    photo_url VARCHAR
(
    500
),
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    account_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    role_id BIGINT REFERENCES roles
(
    id
) ON DELETE RESTRICT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT ck_user_email_format CHECK
(
    email
    LIKE
    '%@%.%'
),
    CONSTRAINT ck_user_display_name_not_empty CHECK
(
    TRIM
(
    display_name
) <> '')
    );

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_users_account_enabled ON users(account_enabled) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_users_is_email_verified ON users(is_email_verified);
CREATE INDEX IF NOT EXISTS idx_users_deleted ON users(deleted);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at DESC) WHERE deleted = FALSE;

-- ==================== USER_AUTHORITIES TABLE ====================
CREATE TABLE IF NOT EXISTS user_authorities
(
    user_id
    BIGINT
    NOT
    NULL
    REFERENCES
    users
(
    user_id
) ON DELETE CASCADE,
    authority VARCHAR
(
    100
) NOT NULL,
    PRIMARY KEY
(
    user_id,
    authority
),
    CONSTRAINT ck_authority_not_empty CHECK
(
    TRIM
(
    authority
) <> '')
    );

CREATE INDEX IF NOT EXISTS idx_user_authorities_authority ON user_authorities(authority);

-- ==================== CREDENTIALS TABLE ====================
CREATE TABLE IF NOT EXISTS credentials
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    name
    VARCHAR
(
    100
) NOT NULL,
    name_normalized VARCHAR
(
    100
) NOT NULL UNIQUE,
    description TEXT,
    is_predefined BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT ck_credential_name_not_empty CHECK
(
    TRIM
(
    name
) <> ''),
    CONSTRAINT ck_credential_normalized_not_empty CHECK
(
    TRIM
(
    name_normalized
) <> '')
    );

CREATE INDEX IF NOT EXISTS idx_credentials_name_normalized ON credentials(name_normalized);
CREATE INDEX IF NOT EXISTS idx_credentials_is_predefined ON credentials(is_predefined) WHERE is_predefined = TRUE;
CREATE INDEX IF NOT EXISTS idx_credentials_deleted ON credentials(deleted) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_credentials_created_at ON credentials(created_at DESC) WHERE deleted = FALSE;

-- ==================== SPECIALTIES TABLE ====================
CREATE TABLE IF NOT EXISTS specialties
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    name
    VARCHAR
(
    100
) NOT NULL,
    name_normalized VARCHAR
(
    100
) NOT NULL UNIQUE,
    description TEXT,
    is_predefined BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT ck_specialty_name_not_empty CHECK
(
    TRIM
(
    name
) <> ''),
    CONSTRAINT ck_specialty_normalized_not_empty CHECK
(
    TRIM
(
    name_normalized
) <> '')
    );

CREATE INDEX IF NOT EXISTS idx_specialties_name_normalized ON specialties(name_normalized);
CREATE INDEX IF NOT EXISTS idx_specialties_is_predefined ON specialties(is_predefined) WHERE is_predefined = TRUE;
CREATE INDEX IF NOT EXISTS idx_specialties_deleted ON specialties(deleted) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_specialties_created_at ON specialties(created_at DESC) WHERE deleted = FALSE;

-- ==================== TOKENS TABLE ====================
CREATE TABLE IF NOT EXISTS tokens
(
    token_id
    BIGSERIAL
    PRIMARY
    KEY,
    email
    VARCHAR
(
    255
) NOT NULL,
    hashed_otp VARCHAR
(
    255
) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_email_format CHECK
(
    email
    LIKE
    '%@%.%'
),
    CONSTRAINT ck_expires_after_created CHECK
(
    expires_at >
    created_at
)
    );

CREATE INDEX IF NOT EXISTS idx_tokens_email ON tokens(email) WHERE is_verified = FALSE AND expires_at > CURRENT_TIMESTAMP;
CREATE INDEX IF NOT EXISTS idx_tokens_expires_at ON tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_tokens_created_at ON tokens(created_at DESC);

-- =====================================================
-- Migration completed successfully
-- =====================================================

