-- =====================================================
-- V6__Insert_initial_subscription_prices.sql
-- Seed data for subscription pricing
-- =====================================================
-- This migration populates the subscription_prices table
-- with pricing for each subscription plan and billing interval
--
-- Amounts are stored in minor units (cents/paise):
-- - 999 = $9.99 USD
-- - 9999 = $99.99 USD
-- - 19999 = $199.99 USD
-- =====================================================

-- Insert subscription prices
-- Format: INSERT INTO subscription_prices (plan_id, billing_interval, currency, amount_in_minor_units, stripe_product_id, stripe_price_id, active, created_at, last_modified_at, deleted)

-- FREE Plan Prices
INSERT INTO subscription_prices (plan_id, billing_interval, currency, amount_in_minor_units, stripe_product_id,
                                 stripe_price_id, active, created_at, last_modified_at, deleted)
VALUES ((SELECT subscription_plan_id FROM subscription_plans WHERE code = 'FREE' AND deleted = FALSE),
        'MONTHLY',
        'usd',
        0,
        'prod_free_monthly',
        'price_free_monthly',
        TRUE,
        NOW(),
        NOW(),
        FALSE),
       ((SELECT subscription_plan_id FROM subscription_plans WHERE code = 'FREE' AND deleted = FALSE),
        'YEARLY',
        'usd',
        0,
        'prod_free_monthly',
        'price_free_yearly',
        TRUE,
        NOW(),
        NOW(),
        FALSE);

-- PRO Plan Prices (with user-provided Stripe IDs)
INSERT INTO subscription_prices (plan_id, billing_interval, currency, amount_in_minor_units, stripe_product_id,
                                 stripe_price_id, active, created_at, last_modified_at, deleted)
VALUES ((SELECT subscription_plan_id FROM subscription_plans WHERE code = 'PRO' AND deleted = FALSE),
        'MONTHLY',
        'usd',
        999,
        'prod_UJd2FQsNQ7bXRi',
        'price_1TKzm2GvbFqTmqQHV78dUPqK',
        TRUE,
        '2026-04-22 21:36:10.669327'::TIMESTAMP,
        '2026-04-22 21:36:10.669327'::TIMESTAMP,
        FALSE),
       ((SELECT subscription_plan_id FROM subscription_plans WHERE code = 'PRO' AND deleted = FALSE),
        'YEARLY',
        'usd',
        9999,
        'prod_UJd2FQsNQ7bXRi',
        'price_1TKzm2GvbFqTmqQH0J4ZpWsq',
        TRUE,
        '2026-04-22 21:36:10.669327'::TIMESTAMP,
        '2026-04-22 21:36:10.669327'::TIMESTAMP,
        FALSE);

-- ELITE Plan Prices
INSERT INTO subscription_prices (plan_id, billing_interval, currency, amount_in_minor_units, stripe_product_id,
                                 stripe_price_id, active, created_at, last_modified_at, deleted)
VALUES ((SELECT subscription_plan_id FROM subscription_plans WHERE code = 'ELITE' AND deleted = FALSE),
        'MONTHLY',
        'usd',
        1999,
        'prod_elite_monthly',
        'price_elite_monthly',
        TRUE,
        NOW(),
        NOW(),
        FALSE),
       ((SELECT subscription_plan_id FROM subscription_plans WHERE code = 'ELITE' AND deleted = FALSE),
        'YEARLY',
        'usd',
        19999,
        'prod_elite_yearly',
        'price_elite_yearly',
        TRUE,
        NOW(),
        NOW(),
        FALSE);

-- =====================================================
-- Verify inserts
-- =====================================================
-- SELECT
--     sp.id,
--     spl.code as plan_code,
--     sp.billing_interval,
--     sp.currency,
--     sp.amount_in_minor_units,
--     sp.stripe_product_id,
--     sp.stripe_price_id,
--     sp.active
-- FROM subscription_prices sp
-- INNER JOIN subscription_plans spl ON sp.plan_id = spl.subscription_plan_id
-- WHERE sp.deleted = FALSE
-- ORDER BY spl.code, sp.billing_interval;
-- Expected: 8 records (3 plans × 2 billing intervals, minus 1 FREE YEARLY = 5)

-- =====================================================
-- Price Reference (in USD)
-- =====================================================
-- FREE MONTHLY: $0.00 (0 cents)
-- FREE YEARLY: $0.00 (0 cents)
-- PRO MONTHLY: $9.99 (999 cents)
-- PRO YEARLY: $99.99 (9999 cents)
-- ELITE MONTHLY: $19.99 (1999 cents)
-- ELITE YEARLY: $199.99 (19999 cents)

