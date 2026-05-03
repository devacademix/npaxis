-- =====================================================
-- V9__Refactor_subscriptions_for_multiple_per_user.sql
-- Refactor subscription system to support multiple subscriptions per user
-- =====================================================
-- This migration:
-- 1. Removes UNIQUE constraint on preceptor_id
-- 2. Adds lifecycle tracking columns (start_date, end_date, cancelled_at, cancel_date)
-- 3. Adds is_active flag for application-level one-active enforcement
-- 4. Creates optimized indexes for new query patterns
-- 5. Backfills existing data with sensible defaults
-- =====================================================

-- =====================================================
-- Step 1: Add new columns to preceptor_subscriptions table
-- =====================================================

-- Add start_date column (when subscription lifecycle began)
ALTER TABLE preceptor_subscriptions
    ADD COLUMN IF NOT EXISTS start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add end_date column (when subscription lifecycle ended, nullable for active subscriptions)
ALTER TABLE preceptor_subscriptions
    ADD COLUMN IF NOT EXISTS end_date TIMESTAMP;

-- Add cancelled_at column (when user explicitly cancelled, distinct from end_date)
ALTER TABLE preceptor_subscriptions
    ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP;

-- Add cancel_date column (when subscription will end at period end for graceful cancellation)
ALTER TABLE preceptor_subscriptions
    ADD COLUMN IF NOT EXISTS cancel_date TIMESTAMP;

-- Add is_active boolean flag (application-enforced one-active-per-user)
-- This allows us to track active vs historical subscriptions
ALTER TABLE preceptor_subscriptions
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

-- =====================================================
-- Step 2: Remove UNIQUE constraints on preceptor_id
-- =====================================================
-- Get the constraint name and drop it
DO
$$
BEGIN
    IF
EXISTS (
        SELECT constraint_name
        FROM information_schema.table_constraints
        WHERE table_name = 'preceptor_subscriptions'
        AND constraint_type = 'UNIQUE'
        AND constraint_name LIKE '%preceptor_id%'
    ) THEN
        EXECUTE 'ALTER TABLE preceptor_subscriptions DROP CONSTRAINT ' ||
                (SELECT constraint_name
                 FROM information_schema.table_constraints
                 WHERE table_name = 'preceptor_subscriptions'
                 AND constraint_type = 'UNIQUE'
                 AND constraint_name LIKE '%preceptor_id%' LIMIT 1);
END IF;
END $$;

-- =====================================================
-- Step 3: Drop and recreate indexes for new query patterns
-- =====================================================

-- Drop old indexes if they exist (safe with IF EXISTS)
DROP INDEX IF EXISTS idx_preceptor_subscriptions_preceptor;
DROP INDEX IF EXISTS idx_preceptor_subscription_preceptor;

-- Create composite index for finding active subscriptions by preceptor
-- This is the most common query: find active subscription for a user
CREATE INDEX IF NOT EXISTS idx_preceptor_subscription_active
    ON preceptor_subscriptions (preceptor_id, is_active)
    WHERE is_active = TRUE;

-- Create index for subscription history queries (most recent first)
CREATE INDEX IF NOT EXISTS idx_preceptor_subscription_history
    ON preceptor_subscriptions (preceptor_id, created_at DESC)
    WHERE deleted = FALSE;

-- Create index for status queries with is_active flag
CREATE INDEX IF NOT EXISTS idx_preceptor_subscription_status_active
    ON preceptor_subscriptions (status, is_active);

-- Keep existing Stripe subscription indexes for webhook processing
-- (stripe_subscription_id remains UNIQUE due to Stripe constraint)

-- =====================================================
-- Step 4: Backfill is_active flag for existing records
-- =====================================================
-- Set is_active = TRUE for ACTIVE, TRIALING subscriptions
UPDATE preceptor_subscriptions
SET is_active = TRUE
WHERE status IN ('ACTIVE', 'TRIALING')
  AND is_active IS NOT TRUE;

-- Set is_active = FALSE for CANCELED, PAST_DUE, UNPAID, INCOMPLETE subscriptions
UPDATE preceptor_subscriptions
SET is_active = FALSE
WHERE status NOT IN ('ACTIVE', 'TRIALING')
  AND is_active IS NOT FALSE;

-- =====================================================
-- Step 5: Backfill lifecycle dates for existing records
-- =====================================================
-- Backfill start_date from created_at if null
UPDATE preceptor_subscriptions
SET start_date = created_at
WHERE start_date IS NULL;

-- Backfill end_date from canceled_at for cancelled subscriptions
UPDATE preceptor_subscriptions
SET end_date = COALESCE(canceled_at, current_period_end)
WHERE end_date IS NULL
  AND status IN ('CANCELED', 'PAST_DUE', 'UNPAID');

-- =====================================================
-- Step 6: Verify data integrity
-- =====================================================
-- Check for any preceptor with multiple active subscriptions (should be 0)
-- This is only for validation and can be checked after migration
-- SELECT preceptor_id, COUNT(*) as active_count
-- FROM preceptor_subscriptions
-- WHERE is_active = TRUE AND deleted = FALSE
-- GROUP BY preceptor_id
-- HAVING COUNT(*) > 1;

-- =====================================================
-- Step 7: Create subscription_events table for audit logging
-- =====================================================
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
    preceptor_id BIGINT NOT NULL REFERENCES preceptors
(
    user_id
)
  ON DELETE CASCADE,
    event_type VARCHAR
(
    50
) NOT NULL, -- CREATED, ACTIVATED, DEACTIVATED, CANCELLED, UPDATED, EXPIRED
    stripe_event_id VARCHAR
(
    255
),
    details JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Create indexes for subscription_events queries
CREATE INDEX IF NOT EXISTS idx_subscription_events_subscription
    ON subscription_events (preceptor_subscription_id);

CREATE INDEX IF NOT EXISTS idx_subscription_events_preceptor
    ON subscription_events (preceptor_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_subscription_events_stripe
    ON subscription_events (stripe_event_id)
    WHERE stripe_event_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_subscription_events_type
    ON subscription_events (event_type, created_at DESC);

-- =====================================================
-- Step 8: Create trigger to log subscription state changes (optional)
-- =====================================================
-- This trigger automatically creates audit records when subscription status changes
-- Disabled by default - can be enabled if needed for full audit trail
-- To enable: uncomment and run separately

/*
CREATE OR REPLACE FUNCTION log_subscription_event()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status IS DISTINCT FROM OLD.status THEN
        INSERT INTO subscription_events
        (preceptor_subscription_id, preceptor_id, event_type, details, created_at)
        VALUES
        (NEW.preceptor_subscription_id, NEW.preceptor_id,
         'UPDATED',
         jsonb_build_object('old_status', OLD.status, 'new_status', NEW.status),
         CURRENT_TIMESTAMP);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_log_subscription_event ON preceptor_subscriptions;
CREATE TRIGGER trigger_log_subscription_event
AFTER UPDATE ON preceptor_subscriptions
FOR EACH ROW
EXECUTE FUNCTION log_subscription_event();
*/

-- =====================================================
-- Migration completed successfully
-- =====================================================

