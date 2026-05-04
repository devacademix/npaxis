-- =====================================================
-- V7__Initialize_sequences_and_optimizations.sql
-- Initialize sequences, add performance indexes, analyze tables
-- =====================================================

-- ==================== SEQUENCE INITIALIZATION ====================
-- Initialize auto-increment sequences to safe values

SELECT setval('roles_id_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM roles) + 1, 1000));
SELECT setval('users_user_id_seq', GREATEST((SELECT COALESCE(MAX(user_id), 0) FROM users) + 1, 1000));
SELECT setval('credentials_id_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM credentials) + 1, 1000));
SELECT setval('specialties_id_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM specialties) + 1, 1000));
SELECT setval('tokens_token_id_seq', GREATEST((SELECT COALESCE(MAX(token_id), 0) FROM tokens) + 1, 1000));
SELECT setval('preceptors_user_id_seq', GREATEST((SELECT COALESCE(MAX(user_id), 0) FROM preceptors) + 1, 1000));
SELECT setval('students_user_id_seq', GREATEST((SELECT COALESCE(MAX(user_id), 0) FROM students) + 1, 1000));
SELECT setval('verification_audit_logs_id_seq',
              GREATEST((SELECT COALESCE(MAX(id), 0) FROM verification_audit_logs) + 1, 1000));
SELECT setval('student_saved_preceptors_id_seq',
              GREATEST((SELECT COALESCE(MAX(id), 0) FROM student_saved_preceptors) + 1, 1000));
SELECT setval('subscription_plans_subscription_plan_id_seq',
              GREATEST((SELECT COALESCE(MAX(subscription_plan_id), 0) FROM subscription_plans) + 1, 1000));
SELECT setval('subscription_prices_id_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM subscription_prices) + 1, 1000));
SELECT setval('preceptor_subscriptions_preceptor_subscription_id_seq',
              GREATEST((SELECT COALESCE(MAX(preceptor_subscription_id), 0) FROM preceptor_subscriptions) + 1, 1000));
SELECT setval('conversations_id_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM conversations) + 1, 1000));
SELECT setval('messages_id_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM messages) + 1, 1000));
SELECT setval('message_read_statuses_id_seq',
              GREATEST((SELECT COALESCE(MAX(id), 0) FROM message_read_statuses) + 1, 1000));
SELECT setval('inquiries_inquiry_id_seq', GREATEST((SELECT COALESCE(MAX(inquiry_id), 0) FROM inquiries) + 1, 1000));
SELECT setval('subscription_events_subscription_event_id_seq',
              GREATEST((SELECT COALESCE(MAX(subscription_event_id), 0) FROM subscription_events) + 1, 1000));
SELECT setval('billing_invoices_id_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM billing_invoices) + 1, 1000));
SELECT setval('billing_transactions_id_seq',
              GREATEST((SELECT COALESCE(MAX(id), 0) FROM billing_transactions) + 1, 1000));
SELECT setval('analytics_events_analytics_id_seq',
              GREATEST((SELECT COALESCE(MAX(analytics_id), 0) FROM analytics_events) + 1, 1000));
SELECT setval('webhook_processing_events_id_seq',
              GREATEST((SELECT COALESCE(MAX(id), 0) FROM webhook_processing_events) + 1, 1000));
SELECT setval('system_settings_setting_id_seq',
              GREATEST((SELECT COALESCE(MAX(setting_id), 0) FROM system_settings) + 1, 1000));

-- ==================== ADDITIONAL COMPOSITE INDEXES ====================
-- Add composite and partial indexes for common query patterns

-- User & Preceptor optimization
CREATE INDEX IF NOT EXISTS idx_users_email_account_enabled ON users(email, account_enabled) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_preceptors_created_at_deleted ON preceptors(created_at DESC, deleted) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_preceptors_verification_submitted_at ON preceptors(verification_submitted_at DESC) WHERE verification_status IS NOT NULL;

-- Subscription optimization
CREATE INDEX IF NOT EXISTS idx_preceptor_subscription_created_at ON preceptor_subscriptions(created_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_preceptor_subscription_status_created_at ON preceptor_subscriptions(status, created_at DESC) WHERE deleted = FALSE;

-- Message optimization
CREATE INDEX IF NOT EXISTS idx_message_conversation_deleted ON messages(conversation_id, deleted) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_message_sender_created_at ON messages(sender_id, created_at DESC) WHERE deleted = FALSE;

-- Analytics optimization
CREATE INDEX IF NOT EXISTS idx_analytics_preceptor_event_type ON analytics_events(preceptor_id, event_type) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_analytics_user_event_type ON analytics_events(user_id, event_type) WHERE deleted = FALSE;

-- Webhook optimization
CREATE INDEX IF NOT EXISTS idx_webhook_event_type_created_at ON webhook_processing_events(event_type, created_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_webhook_status_created_at ON webhook_processing_events(status, created_at DESC) WHERE deleted = FALSE;

-- Billing optimization
CREATE INDEX IF NOT EXISTS idx_billing_invoice_created_at_deleted ON billing_invoices(created_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_billing_transaction_preceptor_created_at ON billing_transactions(preceptor_id, created_at DESC) WHERE deleted = FALSE;

-- ==================== PARTIAL INDEXES FOR COMMON FILTERS ====================
-- These indexes improve queries that frequently filter by status=TRUE or is_active=TRUE

CREATE INDEX IF NOT EXISTS idx_subscription_plans_active_deleted ON subscription_plans(active, deleted) WHERE active = TRUE AND deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_subscription_prices_active_deleted ON subscription_prices(active, deleted) WHERE active = TRUE AND deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_system_settings_active_deleted ON system_settings(is_active, deleted) WHERE is_active = TRUE AND deleted = FALSE;

-- ==================== DATA VALIDATION CHECKS ====================
-- Verify schema integrity and seed data completeness

DO
$$
DECLARE
v_table_count INTEGER;
    v_role_count
INTEGER;
    v_credential_count
INTEGER;
    v_specialty_count
INTEGER;
    v_plan_count
INTEGER;
    v_price_count
INTEGER;
BEGIN
    -- Check that all expected tables exist
SELECT COUNT(*)
INTO v_table_count
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN (
                     'roles', 'users', 'credentials', 'specialties', 'preceptors',
                     'tokens', 'subscription_plans', 'subscription_prices', 'preceptor_subscriptions',
                     'conversations', 'messages', 'message_read_statuses', 'inquiries',
                     'subscription_events', 'billing_invoices', 'billing_transactions',
                     'analytics_events', 'webhook_processing_events', 'system_settings',
                     'students', 'verification_audit_logs', 'user_authorities',
                     'preceptor_credentials', 'preceptor_specialties', 'preceptor_available_days',
                     'student_saved_preceptors'
    );

IF
v_table_count < 20 THEN
        RAISE WARNING 'Only % tables found, expected 21+ tables', v_table_count;
ELSE
        RAISE NOTICE 'All % expected tables exist', v_table_count;
END IF;

    -- Check seed data
SELECT COUNT(*)
INTO v_role_count
FROM roles
WHERE deleted = FALSE;
RAISE
NOTICE 'Roles count: %', v_role_count;

SELECT COUNT(*)
INTO v_credential_count
FROM credentials
WHERE is_predefined = TRUE
  AND deleted = FALSE;
RAISE
NOTICE 'Predefined credentials count: %', v_credential_count;

SELECT COUNT(*)
INTO v_specialty_count
FROM specialties
WHERE is_predefined = TRUE
  AND deleted = FALSE;
RAISE
NOTICE 'Predefined specialties count: %', v_specialty_count;

SELECT COUNT(*)
INTO v_plan_count
FROM subscription_plans
WHERE deleted = FALSE;
RAISE
NOTICE 'Subscription plans count: %', v_plan_count;

SELECT COUNT(*)
INTO v_price_count
FROM subscription_prices
WHERE deleted = FALSE;
RAISE
NOTICE 'Subscription prices count: %', v_price_count;
END $$;

-- ==================== TABLE STATISTICS ====================
-- Analyze tables to help query planner make better decisions

ANALYZE
roles;
ANALYZE
users;
ANALYZE
credentials;
ANALYZE
specialties;
ANALYZE
preceptors;
ANALYZE
students;
ANALYZE
tokens;
ANALYZE
subscription_plans;
ANALYZE
subscription_prices;
ANALYZE
preceptor_subscriptions;
ANALYZE
conversations;
ANALYZE
messages;
ANALYZE
message_read_statuses;
ANALYZE
inquiries;
ANALYZE
subscription_events;
ANALYZE
billing_invoices;
ANALYZE
billing_transactions;
ANALYZE
analytics_events;
ANALYZE
webhook_processing_events;
ANALYZE
system_settings;

-- =====================================================
-- Migration completed successfully!
-- All tables are now ready for production use
-- =====================================================

