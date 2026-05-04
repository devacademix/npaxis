-- =====================================================
-- V5__Create_billing_analytics_webhook_and_system_entities.sql
-- Subscription events, billing, analytics, webhooks, system settings
-- =====================================================

-- ==================== SUBSCRIPTION_EVENTS TABLE ====================
CREATE TABLE IF NOT EXISTS subscription_events
(
    subscription_event_id
    BIGSERIAL
    PRIMARY
    KEY,
    preceptor_subscription_id
    BIGINT
    NOT
    NULL
    REFERENCES
    preceptor_subscriptions
(
    preceptor_subscription_id
) ON DELETE CASCADE,
    preceptor_id BIGINT NOT NULL,
    event_type VARCHAR
(
    50
) NOT NULL,
    stripe_event_id VARCHAR
(
    255
),
    details JSONB,
    status VARCHAR
(
    20
) DEFAULT 'SUCCESS',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_event_type CHECK
(
    event_type
    IN
(
    'CREATED',
    'ACTIVATED',
    'DEACTIVATED',
    'CANCELLED',
    'UPDATED',
    'EXPIRED',
    'PAYMENT_FAILED'
))
    );

CREATE INDEX IF NOT EXISTS idx_subscription_events_subscription ON subscription_events(preceptor_subscription_id);
CREATE INDEX IF NOT EXISTS idx_subscription_events_preceptor ON subscription_events(preceptor_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_subscription_events_stripe ON subscription_events(stripe_event_id) WHERE stripe_event_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_subscription_events_type ON subscription_events(event_type, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_subscription_events_created_at ON subscription_events(created_at DESC);

-- ==================== BILLING_INVOICES TABLE ====================
CREATE TABLE IF NOT EXISTS billing_invoices
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
    stripe_invoice_id VARCHAR
(
    120
) NOT NULL UNIQUE,
    stripe_subscription_id VARCHAR
(
    120
),
    stripe_customer_id VARCHAR
(
    120
),
    amount_paid_in_minor_units BIGINT NOT NULL,
    amount_due_in_minor_units BIGINT NOT NULL,
    currency VARCHAR
(
    10
) NOT NULL DEFAULT 'usd',
    status VARCHAR
(
    30
) NOT NULL DEFAULT 'DRAFT',
    hosted_invoice_url VARCHAR
(
    500
),
    invoice_pdf_url VARCHAR
(
    500
),
    invoice_created_at TIMESTAMP,
    invoice_paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT ck_billing_inv_status CHECK
(
    status
    IN
(
    'DRAFT',
    'OPEN',
    'PAID',
    'VOID',
    'UNCOLLECTIBLE'
)),
    CONSTRAINT ck_billing_inv_amount_paid CHECK
(
    amount_paid_in_minor_units
    >=
    0
),
    CONSTRAINT ck_billing_inv_amount_due CHECK
(
    amount_due_in_minor_units
    >=
    0
),
    CONSTRAINT ck_billing_inv_currency CHECK
(
    CHAR_LENGTH
(
    currency
) = 3 AND currency = UPPER
(
    currency
)),
    CONSTRAINT ck_billing_inv_paid_date CHECK
(
    invoice_paid_at
    IS
    NULL
    OR
    invoice_paid_at
    >=
    invoice_created_at
)
    );

CREATE INDEX IF NOT EXISTS idx_billing_invoice_preceptor ON billing_invoices(preceptor_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_billing_invoice_stripe_invoice_id ON billing_invoices(stripe_invoice_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_billing_invoice_status ON billing_invoices(status) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_billing_invoice_created_at ON billing_invoices(created_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_billing_invoice_invoice_paid_at ON billing_invoices(invoice_paid_at DESC) WHERE invoice_paid_at IS NOT NULL;

-- ==================== BILLING_TRANSACTIONS TABLE ====================
CREATE TABLE IF NOT EXISTS billing_transactions
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
    stripe_payment_intent_id VARCHAR
(
    120
) UNIQUE,
    stripe_invoice_id VARCHAR
(
    120
),
    stripe_subscription_id VARCHAR
(
    120
),
    amount_in_minor_units BIGINT NOT NULL,
    currency VARCHAR
(
    10
) NOT NULL DEFAULT 'usd',
    status VARCHAR
(
    30
) NOT NULL DEFAULT 'PENDING',
    failure_reason VARCHAR
(
    500
),
    transaction_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT ck_billing_tx_status CHECK
(
    status
    IN
(
    'SUCCEEDED',
    'FAILED',
    'PENDING',
    'REFUNDED',
    'PARTIALLY_REFUNDED'
)),
    CONSTRAINT ck_billing_tx_amount CHECK
(
    amount_in_minor_units
    >=
    0
),
    CONSTRAINT ck_billing_tx_currency CHECK
(
    CHAR_LENGTH
(
    currency
) = 3 AND currency = UPPER
(
    currency
)),
    CONSTRAINT ck_billing_tx_transaction_at CHECK
(
    transaction_at
    <=
    CURRENT_TIMESTAMP
)
    );

CREATE INDEX IF NOT EXISTS idx_tx_preceptor ON billing_transactions(preceptor_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_tx_stripe_payment_intent ON billing_transactions(stripe_payment_intent_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_tx_status ON billing_transactions(status) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_tx_created_at ON billing_transactions(created_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_tx_transaction_at ON billing_transactions(transaction_at DESC) WHERE deleted = FALSE;

-- ==================== ANALYTICS_EVENTS TABLE ====================
CREATE TABLE IF NOT EXISTS analytics_events
(
    analytics_id
    BIGSERIAL
    PRIMARY
    KEY,
    event_type
    VARCHAR
(
    50
) NOT NULL,
    preceptor_id BIGINT REFERENCES preceptors
(
    user_id
) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users
(
    user_id
)
  ON DELETE CASCADE,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
    );

CREATE INDEX IF NOT EXISTS idx_analytics_event_type ON analytics_events(event_type) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_analytics_preceptor ON analytics_events(preceptor_id, created_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_analytics_user ON analytics_events(user_id, created_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_analytics_created_at ON analytics_events(created_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_analytics_deleted ON analytics_events(deleted) WHERE deleted = FALSE;

-- ==================== WEBHOOK_PROCESSING_EVENTS TABLE ====================
CREATE TABLE IF NOT EXISTS webhook_processing_events
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    event_id
    VARCHAR
(
    120
) NOT NULL UNIQUE,
    event_type VARCHAR
(
    100
) NOT NULL,
    preceptor_id BIGINT REFERENCES preceptors
(
    user_id
) ON DELETE CASCADE,
    stripe_customer_id VARCHAR
(
    120
),
    payload TEXT NOT NULL,
    status VARCHAR
(
    20
) NOT NULL DEFAULT 'PENDING',
    processed_at TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP,
    livemode BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT ck_webhook_status CHECK
(
    status
    IN
(
    'PENDING',
    'SUCCESS',
    'FAILED_RETRYING',
    'DEAD_LETTER'
)),
    CONSTRAINT ck_webhook_retry_count CHECK
(
    retry_count
    >=
    0
)
    );

CREATE INDEX IF NOT EXISTS idx_webhook_event_id ON webhook_processing_events(event_id);
CREATE INDEX IF NOT EXISTS idx_webhook_event_type ON webhook_processing_events(event_type) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_webhook_status_retry ON webhook_processing_events(status, retry_count) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_webhook_preceptor ON webhook_processing_events(preceptor_id);
CREATE INDEX IF NOT EXISTS idx_webhook_next_retry ON webhook_processing_events(next_retry_at) WHERE status = 'FAILED_RETRYING' AND deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_webhook_created_at ON webhook_processing_events(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_webhook_deleted ON webhook_processing_events(deleted) WHERE deleted = FALSE;

-- ==================== SYSTEM_SETTINGS TABLE ====================
CREATE TABLE IF NOT EXISTS system_settings
(
    setting_id
    BIGSERIAL
    PRIMARY
    KEY,
    setting_key
    VARCHAR
(
    100
) NOT NULL UNIQUE,
    setting_value TEXT,
    description VARCHAR
(
    500
),
    data_type VARCHAR
(
    20
) NOT NULL,
    category VARCHAR
(
    50
) NOT NULL,
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_by BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT uk_system_settings_key UNIQUE
(
    setting_key
),
    CONSTRAINT ck_setting_key_not_empty CHECK
(
    TRIM
(
    setting_key
) <> ''),
    CONSTRAINT ck_data_type CHECK
(
    data_type
    IN
(
    'STRING',
    'INTEGER',
    'BOOLEAN',
    'JSON',
    'DECIMAL'
))
    );

CREATE INDEX IF NOT EXISTS idx_system_settings_key ON system_settings(setting_key);
CREATE INDEX IF NOT EXISTS idx_system_settings_category ON system_settings(category, is_active) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_system_settings_active ON system_settings(is_active) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_system_settings_data_type ON system_settings(data_type) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_system_settings_created_at ON system_settings(created_at DESC) WHERE deleted = FALSE;

-- =====================================================
-- Migration completed successfully
-- =====================================================

