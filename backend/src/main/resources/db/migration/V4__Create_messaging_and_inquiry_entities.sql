-- =====================================================
-- V4__Create_messaging_and_inquiry_entities.sql
-- Conversations, messages, inquiry system
-- =====================================================

-- ==================== CONVERSATIONS TABLE ====================
CREATE TABLE IF NOT EXISTS conversations
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    student_id
    BIGINT
    NOT
    NULL
    REFERENCES
    users
(
    user_id
) ON DELETE RESTRICT,
    preceptor_id BIGINT NOT NULL REFERENCES users
(
    user_id
)
  ON DELETE RESTRICT,
    subject VARCHAR
(
    255
),
    status VARCHAR
(
    50
) NOT NULL DEFAULT 'OPEN',
    last_message_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT ck_conversation_status CHECK
(
    status
    IN
(
    'OPEN',
    'CLOSED',
    'ARCHIVED'
)),
    CONSTRAINT ck_different_users CHECK
(
    student_id
    <>
    preceptor_id
)
    );

CREATE INDEX IF NOT EXISTS idx_conversation_student_id ON conversations(student_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_conversation_preceptor_id ON conversations(preceptor_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_conversation_status ON conversations(status) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_conversation_created_at ON conversations(created_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_conversation_last_message_at ON conversations(last_message_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_conversation_deleted ON conversations(deleted);
CREATE INDEX IF NOT EXISTS idx_conversation_student_preceptor ON conversations(student_id, preceptor_id) WHERE deleted = FALSE;

-- ==================== MESSAGES TABLE ====================
CREATE TABLE IF NOT EXISTS messages
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    conversation_id
    BIGINT
    NOT
    NULL
    REFERENCES
    conversations
(
    id
) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users
(
    user_id
)
  ON DELETE RESTRICT,
    sender_role VARCHAR
(
    50
) NOT NULL,
    content TEXT NOT NULL,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT ck_content_not_empty CHECK
(
    TRIM
(
    content
) <> ''),
    CONSTRAINT ck_read_at_after_created CHECK
(
    read_at
    IS
    NULL
    OR
    read_at
    >=
    created_at
)
    );

CREATE INDEX IF NOT EXISTS idx_message_conversation_id ON messages(conversation_id, created_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_message_sender_id ON messages(sender_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_message_read_at ON messages(read_at) WHERE read_at IS NULL AND deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_message_created_at ON messages(created_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_message_deleted ON messages(deleted);
CREATE INDEX IF NOT EXISTS idx_message_conversation_created ON messages(conversation_id, created_at DESC);

-- ==================== MESSAGE_READ_STATUSES TABLE ====================
CREATE TABLE IF NOT EXISTS message_read_statuses
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    message_id
    BIGINT
    NOT
    NULL
    REFERENCES
    messages
(
    id
) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users
(
    user_id
)
  ON DELETE CASCADE,
    read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_message_user_read_status UNIQUE
(
    message_id,
    user_id
)
    );

CREATE INDEX IF NOT EXISTS idx_read_status_message_id ON message_read_statuses(message_id);
CREATE INDEX IF NOT EXISTS idx_read_status_user_id ON message_read_statuses(user_id);
CREATE INDEX IF NOT EXISTS idx_read_status_read_at ON message_read_statuses(read_at DESC);
CREATE INDEX IF NOT EXISTS idx_read_status_created_at ON message_read_statuses(created_at DESC);

-- ==================== INQUIRIES TABLE ====================
CREATE TABLE IF NOT EXISTS inquiries
(
    inquiry_id
    BIGSERIAL
    PRIMARY
    KEY,
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
  ON DELETE RESTRICT,
    subject VARCHAR
(
    255
),
    message TEXT NOT NULL,
    status VARCHAR
(
    50
) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT ck_inquiry_message_not_empty CHECK
(
    TRIM
(
    message
) <> ''),
    CONSTRAINT ck_inquiry_status CHECK
(
    status
    IN
(
    'PENDING',
    'RESPONDED',
    'ACCEPTED',
    'CLOSED',
    'REJECTED'
)),
    CONSTRAINT ck_inquiry_different_users CHECK
(
    student_id
    <>
    preceptor_id
)
    );

CREATE INDEX IF NOT EXISTS idx_preceptor_id ON inquiries(preceptor_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_student_id ON inquiries(student_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_status ON inquiries(status) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_inquiries_created_at ON inquiries(created_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_inquiries_deleted ON inquiries(deleted);
CREATE INDEX IF NOT EXISTS idx_inquiries_student_preceptor ON inquiries(student_id, preceptor_id) WHERE deleted = FALSE;

-- =====================================================
-- Migration completed successfully
-- =====================================================

