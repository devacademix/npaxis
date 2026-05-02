-- =====================================================
-- V7__Add_sequence_optimization.sql
-- Optimize sequence values for further inserts
-- =====================================================
-- This migration optimizes the sequences for all auto-increment
-- sequences to prevent conflicts and ensure smooth operations
-- =====================================================

-- Determine current max values and set sequences appropriately
SELECT setval('credentials_id_seq', (SELECT MAX(id) FROM credentials) + 1);
SELECT setval('specialties_id_seq', (SELECT MAX(id) FROM specialties) + 1);
SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles) + 1);
SELECT setval('users_user_id_seq', (SELECT MAX(user_id) FROM users) + 1);
SELECT setval('subscription_plans_subscription_plan_id_seq', (SELECT MAX(subscription_plan_id) FROM subscription_plans) + 1);
SELECT setval('subscription_prices_id_seq', (SELECT MAX(id) FROM subscription_prices) + 1);
SELECT setval('preceptor_subscriptions_preceptor_subscription_id_seq', (SELECT MAX(preceptor_subscription_id) FROM preceptor_subscriptions) + 1);
SELECT setval('tokens_token_id_seq', (SELECT MAX(token_id) FROM tokens) + 1);
SELECT setval('invoices_invoice_id_seq', (SELECT MAX(invoice_id) FROM invoices) + 1);

-- =====================================================
-- Migration completed successfully
-- =====================================================

