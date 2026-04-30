package com.digitalearn.npaxis.preceptor;

/**
 * Verification status enum for preceptor license workflow.
 * <p>
 * Transition rules:
 * NOT_SUBMITTED → PENDING (preceptor submits)
 * PENDING → APPROVED | REJECTED | CORRECTION_REQUESTED (admin reviews)
 * REJECTED → NOT_SUBMITTED (preceptor can resubmit)
 * CORRECTION_REQUESTED → RESUBMITTED (preceptor corrects)
 * RESUBMITTED → APPROVED | REJECTED (admin reviews again)
 */
public enum VerificationStatus {
    NOT_SUBMITTED,
    PENDING,
    CORRECTION_REQUESTED,
    RESUBMITTED,
    APPROVED,
    REJECTED
}