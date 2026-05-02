-- =====================================================
-- V5__Insert_initial_subscription_plans.sql
-- Seed data for subscription tiers
-- =====================================================
-- This migration populates the subscription_plans table
-- with available subscription tiers (FREE, PRO, ELITE)
-- =====================================================

-- Insert subscription plans
INSERT INTO subscription_plans (code, name, description, active, feature_json, created_at, last_modified_at, deleted)
VALUES
    (
        'FREE',
        'Free Plan',
        'Basic plan with limited features for getting started',
        TRUE,
        '{"features": ["Limited access", "Email support", "Basic reporting"]}',
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'PRO',
        'Pro Plan',
        'Advanced plan with premium features',
        TRUE,
        '{"features": ["Unlimited access", "Priority support", "Advanced analytics"]}',
        '2026-04-22 21:35:54.821204'::TIMESTAMP,
        '2026-04-22 21:35:54.821204'::TIMESTAMP,
        FALSE
    ),
    (
        'ELITE',
        'Elite Plan',
        'Premium plan with exclusive features and dedicated support',
        TRUE,
        '{"features": ["Unlimited access", "24/7 dedicated support", "Advanced analytics", "Custom integrations", "API access"]}',
        NOW(),
        NOW(),
        FALSE
    );

-- =====================================================
-- Verify inserts
-- =====================================================
-- SELECT subscription_plan_id, code, name, active FROM subscription_plans WHERE deleted = FALSE ORDER BY subscription_plan_id;
-- Expected: 3 records (FREE, PRO, ELITE)

