-- =====================================================
-- V6__Insert_seed_data.sql
-- System-wide seed data: roles, credentials, specialties, subscription plans
-- =====================================================

-- ==================== INSERT ROLES ====================
INSERT INTO roles (name, description, created_at, last_modified_at, deleted)
VALUES ('ADMIN', 'Administrator with full system access', NOW(), NOW(), FALSE),
       ('PRECEPTOR', 'Educator and preceptor role', NOW(), NOW(), FALSE),
       ('STUDENT', 'Student learner role', NOW(), NOW(), FALSE),
       ('USER', 'Basic user role', NOW(), NOW(), FALSE)
ON CONFLICT (name) DO NOTHING;

-- ==================== INSERT CREDENTIALS ====================
INSERT INTO credentials (name, name_normalized, description, is_predefined, created_at, last_modified_at, deleted)
VALUES ('MBBS', 'MBBS', 'Bachelor of Medicine, Bachelor of Surgery', TRUE, NOW(), NOW(), FALSE),
       ('MD', 'MD', 'Doctor of Medicine', TRUE, NOW(), NOW(), FALSE),
       ('DO', 'DO', 'Doctor of Osteopathic Medicine', TRUE, NOW(), NOW(), FALSE),
       ('PhD', 'PHD', 'Doctor of Philosophy', TRUE, NOW(), NOW(), FALSE),
       ('DNP', 'DNP', 'Doctor of Nursing Practice', TRUE, NOW(), NOW(), FALSE),
       ('PA-C', 'PA-C', 'Physician Assistant-Certified', TRUE, NOW(), NOW(), FALSE),
       ('NP', 'NP', 'Nurse Practitioner', TRUE, NOW(), NOW(), FALSE),
       ('MSN', 'MSN', 'Master of Science in Nursing', TRUE, NOW(), NOW(), FALSE),
       ('RN', 'RN', 'Registered Nurse', TRUE, NOW(), NOW(), FALSE),
       ('DDS', 'DDS', 'Doctor of Dental Surgery', TRUE, NOW(), NOW(), FALSE),
       ('DVM', 'DVM', 'Doctor of Veterinary Medicine', TRUE, NOW(), NOW(), FALSE),
       ('MPH', 'MPH', 'Master of Public Health', TRUE, NOW(), NOW(), FALSE),
       ('DPT', 'DPT', 'Doctor of Physical Therapy', TRUE, NOW(), NOW(), FALSE),
       ('BCPS', 'BCPS', 'Board Certified Pharmacotherapy Specialist', TRUE, NOW(), NOW(), FALSE),
       ('MBA', 'MBA', 'Master of Business Administration', TRUE, NOW(), NOW(),
        FALSE) ON CONFLICT (name_normalized) DO NOTHING;

-- ==================== INSERT SPECIALTIES ====================
INSERT INTO specialties (name, name_normalized, description, is_predefined, created_at, last_modified_at, deleted)
VALUES ('Cardiology', 'CARDIOLOGY', 'Study and treatment of heart and cardiovascular system diseases', TRUE, NOW(),
        NOW(), FALSE),
       ('Internal Medicine', 'INTERNAL MEDICINE', 'General internal medicine', TRUE, NOW(), NOW(), FALSE),
       ('Pediatrics', 'PEDIATRICS', 'Medical care of infants, children, and adolescents', TRUE, NOW(), NOW(), FALSE),
       ('Surgery', 'SURGERY', 'Operative and surgical treatment', TRUE, NOW(), NOW(), FALSE),
       ('Orthopedic Surgery', 'ORTHOPEDIC SURGERY', 'Surgical treatment of bones and joints', TRUE, NOW(), NOW(),
        FALSE),
       ('Neurology', 'NEUROLOGY', 'Diagnosis and treatment of nervous system disorders', TRUE, NOW(), NOW(), FALSE),
       ('Psychiatry', 'PSYCHIATRY', 'Mental health and behavioral disorders', TRUE, NOW(), NOW(), FALSE),
       ('Obstetrics & Gynecology', 'OBSTETRICS & GYNECOLOGY', 'Pregnancy, childbirth, and reproductive health', TRUE,
        NOW(), NOW(), FALSE),
       ('Dermatology', 'DERMATOLOGY', 'Treatment of skin disorders', TRUE, NOW(), NOW(), FALSE),
       ('Radiology', 'RADIOLOGY', 'Diagnostic imaging and radiological treatment', TRUE, NOW(), NOW(), FALSE),
       ('Pathology', 'PATHOLOGY', 'Study of disease', TRUE, NOW(), NOW(), FALSE),
       ('Anesthesiology', 'ANESTHESIOLOGY', 'Administration of anesthesia', TRUE, NOW(), NOW(), FALSE),
       ('Emergency Medicine', 'EMERGENCY MEDICINE', 'Acute care in emergency settings', TRUE, NOW(), NOW(), FALSE),
       ('Family Medicine', 'FAMILY MEDICINE', 'Comprehensive medical care for families', TRUE, NOW(), NOW(), FALSE),
       ('Oncology', 'ONCOLOGY', 'Treatment and prevention of cancer', TRUE, NOW(), NOW(), FALSE),
       ('Pulmonology', 'PULMONOLOGY', 'Treatment of respiratory diseases', TRUE, NOW(), NOW(), FALSE),
       ('Gastroenterology', 'GASTROENTEROLOGY', 'Treatment of digestive system diseases', TRUE, NOW(), NOW(), FALSE),
       ('Nephrology', 'NEPHROLOGY', 'Treatment of kidney and urinary system diseases', TRUE, NOW(), NOW(), FALSE),
       ('Endocrinology', 'ENDOCRINOLOGY', 'Treatment of endocrine glands and hormone-related disorders', TRUE, NOW(),
        NOW(), FALSE),
       ('Rheumatology', 'RHEUMATOLOGY', 'Treatment of autoimmune and joint disorders', TRUE, NOW(), NOW(), FALSE),
       ('Infectious Diseases', 'INFECTIOUS DISEASES', 'Diagnosis and treatment of infectious diseases', TRUE, NOW(),
        NOW(), FALSE),
       ('Immunology', 'IMMUNOLOGY', 'Study of immune system', TRUE, NOW(), NOW(), FALSE),
       ('Hematology', 'HEMATOLOGY', 'Treatment of blood disorders', TRUE, NOW(), NOW(), FALSE),
       ('Ophthalmology', 'OPHTHALMOLOGY', 'Treatment of eye and vision disorders', TRUE, NOW(), NOW(), FALSE),
       ('Otolaryngology', 'OTOLARYNGOLOGY', 'Treatment of ear, nose, and throat', TRUE, NOW(), NOW(), FALSE),
       ('Urology', 'UROLOGY', 'Treatment of genitourinary system diseases', TRUE, NOW(), NOW(), FALSE),
       ('Neurosurgery', 'NEUROSURGERY', 'Surgical treatment of nervous system disorders', TRUE, NOW(), NOW(), FALSE),
       ('Nursing', 'NURSING', 'Patient care and nursing support', TRUE, NOW(), NOW(), FALSE),
       ('Physical Therapy', 'PHYSICAL THERAPY', 'Rehabilitation and movement therapy', TRUE, NOW(), NOW(), FALSE),
       ('Psychology', 'PSYCHOLOGY', 'Mental health and psychological support', TRUE, NOW(), NOW(),
        FALSE) ON CONFLICT (name_normalized) DO NOTHING;

-- ==================== INSERT SUBSCRIPTION PLANS ====================
INSERT INTO subscription_plans (code, name, description, active, feature_json, created_at, last_modified_at, deleted)
VALUES ('FREE', 'Free Plan', 'Basic plan with limited features', TRUE,
        '{"features":["Limited access","Email support"]}', NOW(), NOW(), FALSE),
       ('PRO', 'Pro Plan', 'Advanced plan with premium features', TRUE,
        '{"features":["Unlimited access","Priority support","Advanced analytics"]}', NOW(), NOW(), FALSE),
       ('ELITE', 'Elite Plan', 'Premium plan with exclusive features', TRUE,
        '{"features":["Unlimited access","24/7 support","Advanced analytics","Custom integrations","API access"]}',
        NOW(), NOW(), FALSE) ON CONFLICT (code) DO NOTHING;

-- ==================== INSERT SUBSCRIPTION PRICES ====================
-- FREE Plan Pricing
INSERT INTO subscription_prices (plan_id, billing_interval, currency, amount_in_minor_units, stripe_product_id,
                                 stripe_price_id, active, created_at, last_modified_at, deleted)
SELECT sp.subscription_plan_id,
       'MONTHLY'::VARCHAR(20), 'usd'::VARCHAR(10), 0::BIGINT, 'prod_free_monthly'::VARCHAR(120), 'price_free_monthly'::VARCHAR(120), TRUE::BOOLEAN, NOW(),
       NOW(),
       FALSE::BOOLEAN
FROM subscription_plans sp
WHERE sp.code = 'FREE'
  AND sp.deleted = FALSE ON CONFLICT (plan_id, billing_interval, currency) DO NOTHING;

INSERT INTO subscription_prices (plan_id, billing_interval, currency, amount_in_minor_units, stripe_product_id,
                                 stripe_price_id, active, created_at, last_modified_at, deleted)
SELECT sp.subscription_plan_id,
       'YEARLY'::VARCHAR(20), 'usd'::VARCHAR(10), 0::BIGINT, 'prod_free_yearly'::VARCHAR(120), 'price_free_yearly'::VARCHAR(120), TRUE::BOOLEAN, NOW(),
       NOW(),
       FALSE::BOOLEAN
FROM subscription_plans sp
WHERE sp.code = 'FREE'
  AND sp.deleted = FALSE ON CONFLICT (plan_id, billing_interval, currency) DO NOTHING;

-- PRO Plan Pricing
INSERT INTO subscription_prices (plan_id, billing_interval, currency, amount_in_minor_units, stripe_product_id,
                                 stripe_price_id, active, created_at, last_modified_at, deleted)
SELECT sp.subscription_plan_id,
       'MONTHLY'::VARCHAR(20), 'usd'::VARCHAR(10), 999::BIGINT, 'prod_pro_monthly'::VARCHAR(120), 'price_pro_monthly'::VARCHAR(120), TRUE::BOOLEAN, NOW(),
       NOW(),
       FALSE::BOOLEAN
FROM subscription_plans sp
WHERE sp.code = 'PRO'
  AND sp.deleted = FALSE ON CONFLICT (plan_id, billing_interval, currency) DO NOTHING;

INSERT INTO subscription_prices (plan_id, billing_interval, currency, amount_in_minor_units, stripe_product_id,
                                 stripe_price_id, active, created_at, last_modified_at, deleted)
SELECT sp.subscription_plan_id,
       'YEARLY'::VARCHAR(20), 'usd'::VARCHAR(10), 9999::BIGINT, 'prod_pro_yearly'::VARCHAR(120), 'price_pro_yearly'::VARCHAR(120), TRUE::BOOLEAN, NOW(),
       NOW(),
       FALSE::BOOLEAN
FROM subscription_plans sp
WHERE sp.code = 'PRO'
  AND sp.deleted = FALSE ON CONFLICT (plan_id, billing_interval, currency) DO NOTHING;

-- =====================================================
-- Migration completed successfully
-- =====================================================

