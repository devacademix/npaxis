-- =====================================================
-- V2__Create_preceptor_and_student_entities.sql
-- Preceptor profiles, students, and their relationships
-- =====================================================

-- ==================== PRECEPTORS TABLE ====================
CREATE TABLE IF NOT EXISTS preceptors
(
    user_id
    BIGINT
    PRIMARY
    KEY
    REFERENCES
    users
(
    user_id
) ON DELETE CASCADE,
    location VARCHAR
(
    150
),
    setting VARCHAR
(
    100
),
    honorarium VARCHAR
(
    100
),
    requirements TEXT,
    phone VARCHAR
(
    20
),

    -- Licensing
    license_number VARCHAR
(
    100
),
    license_state VARCHAR
(
    50
),
    license_file_url VARCHAR
(
    500
),

    -- Status flags
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_premium BOOLEAN NOT NULL DEFAULT FALSE,

    -- Verification workflow
    verification_status VARCHAR
(
    20
) DEFAULT 'NOT_SUBMITTED',
    verification_submitted_at TIMESTAMP,
    verification_reviewed_at TIMESTAMP,
    verification_attempts INTEGER DEFAULT 0,

    -- License correction workflow
    correction_requested_reason TEXT,
    correction_requested_at TIMESTAMP,
    resubmitted_at TIMESTAMP,

    -- Stripe integration
    stripe_customer_id VARCHAR
(
    100
),
    stripe_subscription_id VARCHAR
(
    100
),

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT ck_verification_status CHECK
(
    verification_status
    IN
(
    'NOT_SUBMITTED',
    'PENDING',
    'APPROVED',
    'REJECTED',
    'UNDER_REVIEW',
    'AWAITING_CORRECTION'
))
    );

CREATE INDEX IF NOT EXISTS idx_preceptors_is_verified ON preceptors(is_verified) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_preceptors_is_premium ON preceptors(is_premium) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_preceptors_verification_status ON preceptors(verification_status) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_preceptors_deleted ON preceptors(deleted);
CREATE INDEX IF NOT EXISTS idx_preceptors_created_at ON preceptors(created_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_preceptors_license_number ON preceptors(license_number) WHERE license_number IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_preceptors_stripe_customer_id ON preceptors(stripe_customer_id) WHERE stripe_customer_id IS NOT NULL;

-- ==================== PRECEPTOR_CREDENTIALS TABLE ====================
CREATE TABLE IF NOT EXISTS preceptor_credentials
(
    preceptor_id
    BIGINT
    NOT
    NULL
    REFERENCES
    preceptors
(
    user_id
) ON DELETE CASCADE,
    credential_id BIGINT NOT NULL REFERENCES credentials
(
    id
)
  ON DELETE CASCADE,
    PRIMARY KEY
(
    preceptor_id,
    credential_id
)
    );

CREATE INDEX IF NOT EXISTS idx_preceptor_credentials_credential_id ON preceptor_credentials(credential_id);

-- ==================== PRECEPTOR_SPECIALTIES TABLE ====================
CREATE TABLE IF NOT EXISTS preceptor_specialties
(
    preceptor_id
    BIGINT
    NOT
    NULL
    REFERENCES
    preceptors
(
    user_id
) ON DELETE CASCADE,
    specialty_id BIGINT NOT NULL REFERENCES specialties
(
    id
)
  ON DELETE CASCADE,
    PRIMARY KEY
(
    preceptor_id,
    specialty_id
)
    );

CREATE INDEX IF NOT EXISTS idx_preceptor_specialties_specialty_id ON preceptor_specialties(specialty_id);

-- ==================== PRECEPTOR_AVAILABLE_DAYS TABLE ====================
CREATE TABLE IF NOT EXISTS preceptor_available_days
(
    preceptor_id
    BIGINT
    NOT
    NULL
    REFERENCES
    preceptors
(
    user_id
) ON DELETE CASCADE,
    day VARCHAR
(
    20
) NOT NULL,
    PRIMARY KEY
(
    preceptor_id,
    day
),
    CONSTRAINT ck_day_value CHECK
(
    day
    IN
(
    'MONDAY',
    'TUESDAY',
    'WEDNESDAY',
    'THURSDAY',
    'FRIDAY',
    'SATURDAY',
    'SUNDAY'
))
    );

-- ==================== STUDENTS TABLE ====================
CREATE TABLE IF NOT EXISTS students
(
    user_id
    BIGINT
    PRIMARY
    KEY
    REFERENCES
    users
(
    user_id
) ON DELETE CASCADE,
    university VARCHAR
(
    100
),
    program VARCHAR
(
    100
),
    graduation_year VARCHAR
(
    4
),
    phone VARCHAR
(
    20
),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT ck_graduation_year_format CHECK
(
    graduation_year
    IS
    NULL
    OR
    graduation_year
    ~
    '^\d{4}$'
),
    CONSTRAINT ck_phone_format CHECK
(
    phone
    IS
    NULL
    OR
    phone
    ~
    '^\+?[0-9\.\s\-()]{10,}$'
)
    );

CREATE INDEX IF NOT EXISTS idx_students_university ON students(university) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_students_program ON students(program) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_students_deleted ON students(deleted);
CREATE INDEX IF NOT EXISTS idx_students_created_at ON students(created_at DESC) WHERE deleted = FALSE;

-- ==================== STUDENT_SAVED_PRECEPTORS TABLE ====================
CREATE TABLE IF NOT EXISTS student_saved_preceptors
(
    student_id
    BIGINT
    NOT
    NULL
    REFERENCES
    students
(
    user_id
) ON DELETE CASCADE,
    preceptor_id BIGINT NOT NULL REFERENCES preceptors
(
    user_id
)
  ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    student_id,
    preceptor_id
)
    );

CREATE INDEX IF NOT EXISTS idx_student_saved_preceptors_preceptor_id ON student_saved_preceptors(preceptor_id);
CREATE INDEX IF NOT EXISTS idx_student_saved_preceptors_created_at ON student_saved_preceptors(created_at DESC);

-- ==================== VERIFICATION_AUDIT_LOGS TABLE ====================
CREATE TABLE IF NOT EXISTS verification_audit_logs
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    preceptor_id
    BIGINT
    NOT
    NULL
    REFERENCES
    preceptors
(
    user_id
) ON DELETE CASCADE,
    old_status VARCHAR
(
    20
),
    new_status VARCHAR
(
    20
) NOT NULL,
    change_reason TEXT,
    reviewer_user_id BIGINT REFERENCES users
(
    user_id
)
  ON DELETE SET NULL,
    change_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
    );

CREATE INDEX IF NOT EXISTS idx_verification_audit_preceptor ON verification_audit_logs(preceptor_id, change_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_verification_audit_status ON verification_audit_logs(new_status);
CREATE INDEX IF NOT EXISTS idx_verification_audit_reviewer ON verification_audit_logs(reviewer_user_id);
CREATE INDEX IF NOT EXISTS idx_verification_audit_created_at ON verification_audit_logs(created_at DESC);

-- =====================================================
-- Migration completed successfully
-- =====================================================

