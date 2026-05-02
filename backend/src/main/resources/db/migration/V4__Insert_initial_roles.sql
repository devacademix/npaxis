-- =====================================================
-- V4__Insert_initial_roles.sql
-- Seed data for system roles
-- =====================================================
-- This migration populates the roles table with
-- standard system roles for role-based access control
-- =====================================================

-- Insert standard system roles
INSERT INTO roles (name, description, created_at, last_modified_at, deleted)
VALUES
    (
        'ADMIN',
        'Administrator role with full system access',
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'PRECEPTOR',
        'Preceptor role for medical educators and instructors',
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'STUDENT',
        'Student role for learners using the platform',
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'USER',
        'Basic user role with limited access',
        NOW(),
        NOW(),
        FALSE
    ),
    (
        'SUPER_ADMIN',
        'Super administrator role with complete system access',
        NOW(),
        NOW(),
        FALSE
    );

-- =====================================================
-- Verify inserts
-- =====================================================
-- SELECT COUNT(*) as total_roles FROM roles WHERE deleted = FALSE;
-- Expected: 5 records

