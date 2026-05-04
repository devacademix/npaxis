-- =====================================================
-- V3__Create_subscription_entities.sql
-- Subscription plans, prices, and preceptor subscriptions
-- =====================================================

-- ==================== SUBSCRIPTION_PLANS TABLE ====================
CREATE TABLE IF NOT EXISTS subscription_plans
(
    subscription_plan_id
    BIGSERIAL
    PRIMARY
    KEY,
    code
    VARCHAR
(
    80
) NOT NULL UNIQUE,
    name VARCHAR
(
    120
) NOT NULL,
    description TEXT,
    feature_json TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT ck_plan_code_not_empty CHECK
(
    TRIM
(
    code
) <> ''),
    CONSTRAINT ck_plan_name_not_empty CHECK
(
    TRIM
(
    name
) <> '')
    );

CREATE INDEX IF NOT EXISTS idx_subscription_plans_code ON subscription_plans(code);
CREATE INDEX IF NOT EXISTS idx_subscription_plans_active ON subscription_plans(active) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_subscription_plans_deleted ON subscription_plans(deleted);
CREATE INDEX IF NOT EXISTS idx_subscription_plans_created_at ON subscription_plans(created_at DESC) WHERE deleted = FALSE;

-- ==================== SUBSCRIPTION_PRICES TABLE ====================
CREATE TABLE IF NOT EXISTS subscription_prices
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    plan_id
    BIGINT
    NOT
    NULL
    REFERENCES
    subscription_plans
(
    subscription_plan_id
) ON DELETE RESTRICT,
    billing_interval VARCHAR
(
    20
) NOT NULL,
    currency VARCHAR
(
    10
) NOT NULL DEFAULT 'usd',
    amount_in_minor_units BIGINT NOT NULL,
    stripe_product_id VARCHAR
(
    120
) NOT NULL,
    stripe_price_id VARCHAR
(
    120
) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    UNIQUE
(
    plan_id,
    billing_interval,
    currency
),
    CONSTRAINT ck_billing_interval CHECK
(
    billing_interval
    IN
(
    'MONTHLY',
    'YEARLY',
    'WEEKLY'
)),
    CONSTRAINT ck_currency_code CHECK
(
    CHAR_LENGTH
(
    currency
) = 3 AND currency = UPPER
(
    currency
)),
    CONSTRAINT ck_amount_not_negative CHECK
(
    amount_in_minor_units
    >=
    0
),
    CONSTRAINT ck_stripe_product_id_not_empty CHECK
(
    TRIM
(
    stripe_product_id
) <> ''),
    CONSTRAINT ck_stripe_price_id_not_empty CHECK
(
    TRIM
(
    stripe_price_id
) <> '')
    );

CREATE INDEX IF NOT EXISTS idx_subscription_prices_plan_id ON subscription_prices(plan_id);
CREATE INDEX IF NOT EXISTS idx_subscription_prices_active ON subscription_prices(active) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_subscription_prices_billing_interval ON subscription_prices(billing_interval);
CREATE INDEX IF NOT EXISTS idx_subscription_prices_currency ON subscription_prices(currency);
CREATE INDEX IF NOT EXISTS idx_subscription_prices_deleted ON subscription_prices(deleted);
CREATE INDEX IF NOT EXISTS idx_subscription_prices_stripe_price_id ON subscription_prices(stripe_price_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_subscription_prices_created_at ON subscription_prices(created_at DESC) WHERE deleted = FALSE;

-- ==================== PRECEPTOR_SUBSCRIPTIONS TABLE ====================
CREATE TABLE IF NOT EXISTS preceptor_subscriptions
(
    preceptor_subscription_id
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
    plan_id BIGINT NOT NULL REFERENCES subscription_plans
(
    subscription_plan_id
)
  ON DELETE RESTRICT,
    price_id BIGINT NOT NULL REFERENCES subscription_prices
(
    id
)
  ON DELETE RESTRICT,
    status VARCHAR
(
    50
) NOT NULL DEFAULT 'ACTIVE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Stripe integration
    stripe_customer_id VARCHAR
(
    120
) NOT NULL,
    stripe_subscription_id VARCHAR
(
    120
) NOT NULL UNIQUE,

    -- Period tracking
    current_period_start TIMESTAMP,
    current_period_end TIMESTAMP,
    cancel_at_period_end BOOLEAN DEFAULT FALSE,
    trial_ends_at TIMESTAMP,

    -- Access flags
    access_enabled BOOLEAN NOT NULL DEFAULT FALSE,

    -- Lifecycle tracking
    canceled_at TIMESTAMP,
    canceled_reason VARCHAR
(
    255
),
    last_payment_failure_reason TEXT,
    payment_retry_count INTEGER DEFAULT 0,
    next_billing_date TIMESTAMP,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,
    CONSTRAINT ck_status CHECK
(
    status
    IN
(
    'ACTIVE',
    'TRIALING',
    'PAST_DUE',
    'UNPAID',
    'INCOMPLETE',
    'CANCELED'
)),
    CONSTRAINT ck_period_end_after_start CHECK
(
    current_period_end
    IS
    NULL
    OR
    current_period_end
    >=
    current_period_start
)
    );

CREATE INDEX IF NOT EXISTS idx_preceptor_subscription_active ON preceptor_subscriptions(preceptor_id, is_active) WHERE is_active = TRUE AND deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_preceptor_subscription_history ON preceptor_subscriptions(preceptor_id, created_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_preceptor_subscription_status ON preceptor_subscriptions(status) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_preceptor_subscription_stripe ON preceptor_subscriptions(stripe_subscription_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_preceptor_subscription_stripe_customer ON preceptor_subscriptions(stripe_customer_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_preceptor_subscription_status_active ON preceptor_subscriptions(status, is_active) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_preceptor_subscription_deleted ON preceptor_subscriptions(deleted);
CREATE INDEX IF NOT EXISTS idx_preceptor_subscription_created_at ON preceptor_subscriptions(created_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_preceptor_subscription_next_billing ON preceptor_subscriptions(next_billing_date) WHERE next_billing_date IS NOT NULL AND deleted = FALSE;

-- =====================================================
-- Migration completed successfully
-- =====================================================

