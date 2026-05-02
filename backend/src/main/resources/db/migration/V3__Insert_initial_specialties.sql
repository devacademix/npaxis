-- =====================================================
-- V3__Insert_initial_specialties.sql
-- Seed data for system-predefined medical specialties
-- =====================================================
-- This migration populates the specialties table with
-- standard medical specialties that represent various
-- medical fields and disciplines
-- =====================================================

-- Insert standard medical specialties
INSERT INTO specialties (name, name_normalized, description, is_predefined, created_at, last_modified_at, deleted)
VALUES
    (
        'Cardiology',
        'CARDIOLOGY',
        'Study and treatment of heart and cardiovascular system diseases',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Internal Medicine',
        'INTERNAL MEDICINE',
        'General internal medicine focusing on adult disease diagnosis and treatment',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Pediatrics',
        'PEDIATRICS',
        'Medical care and treatment of infants, children, and adolescents',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Surgery',
        'SURGERY',
        'Operative and surgical treatment of diseases and injuries',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Orthopedic Surgery',
        'ORTHOPEDIC SURGERY',
        'Surgical treatment of bones, joints, and musculoskeletal disorders',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Neurology',
        'NEUROLOGY',
        'Diagnosis and treatment of nervous system disorders',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Psychiatry',
        'PSYCHIATRY',
        'Diagnosis and treatment of mental health and behavioral disorders',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Obstetrics & Gynecology',
        'OBSTETRICS & GYNECOLOGY',
        'Medical care during pregnancy, childbirth, and reproductive health',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Dermatology',
        'DERMATOLOGY',
        'Treatment of skin, hair, and nail disorders',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Radiology',
        'RADIOLOGY',
        'Diagnostic imaging and radiological treatment',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Pathology',
        'PATHOLOGY',
        'Study of disease and examination of tissues and specimens',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Anesthesiology',
        'ANESTHESIOLOGY',
        'Administration of anesthesia and perioperative care',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Emergency Medicine',
        'EMERGENCY MEDICINE',
        'Acute care treatment in emergency settings',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Family Medicine',
        'FAMILY MEDICINE',
        'Comprehensive medical care for individuals and families',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Oncology',
        'ONCOLOGY',
        'Treatment and prevention of cancer',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Pulmonology',
        'PULMONOLOGY',
        'Treatment of respiratory and lung diseases',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Gastroenterology',
        'GASTROENTEROLOGY',
        'Treatment of digestive system diseases',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Nephrology',
        'NEPHROLOGY',
        'Treatment of kidney and urinary system diseases',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Endocrinology',
        'ENDOCRINOLOGY',
        'Treatment of endocrine glands and hormone-related disorders',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Rheumatology',
        'RHEUMATOLOGY',
        'Treatment of autoimmune and joint disorders',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Infectious Diseases',
        'INFECTIOUS DISEASES',
        'Diagnosis and treatment of infectious diseases',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Immunology',
        'IMMUNOLOGY',
        'Study of immune system and immune disorders',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Hematology',
        'HEMATOLOGY',
        'Treatment of blood disorders and blood cancers',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Ophthalmology',
        'OPHTHALMOLOGY',
        'Treatment of eye and vision disorders',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Otolaryngology',
        'OTOLARYNGOLOGY',
        'Treatment of ear, nose, and throat conditions',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Urology',
        'UROLOGY',
        'Treatment of genitourinary system diseases',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Neurosurgery',
        'NEUROSURGERY',
        'Surgical treatment of nervous system disorders',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Psychiatry & Psychology',
        'PSYCHIATRY & PSYCHOLOGY',
        'Mental health diagnosis and treatment',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Nursing',
        'NURSING',
        'Patient care and nursing support',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'Physical Therapy',
        'PHYSICAL THERAPY',
        'Rehabilitation and movement therapy',
        TRUE,
        NOW(),
        NOW(),
        FALSE
    );

-- =====================================================
-- Verify inserts
-- =====================================================
-- SELECT COUNT(*) as total_specialties FROM specialties WHERE is_predefined = TRUE AND deleted = FALSE;
-- Expected: 30 records

