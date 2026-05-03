package com.digitalearn.npaxis.subscription.core;

/**
 * Types of events that can occur in a subscription lifecycle
 */
public enum SubscriptionEventType {
    // Lifecycle events
    CREATED,           // Subscription initially created
    ACTIVATED,         // Subscription activated (e.g., after trial)
    CANCELLED,         // User explicitly cancelled
    EXPIRED,           // Subscription period ended
    REACTIVATED,       // User resubscribed after cancellation

    // Update events
    PLAN_UPGRADED,     // User upgraded to higher tier
    PLAN_DOWNGRADED,   // User downgraded to lower tier
    PLAN_CHANGED,      // User changed plan
    BILLING_INTERVAL_CHANGED, // User changed billing frequency

    // Payment/Status events
    PAYMENT_FAILED,    // Payment attempt failed
    PAYMENT_SUCCEEDED, // Payment attempt succeeded
    STATUS_CHANGED,    // General status change (ACTIVE -> PAST_DUE, etc.)
    PAST_DUE,          // Subscription marked as past due
    UNPAID,            // Subscription marked as unpaid

    // Admin/System events
    MANUALLY_ACTIVATED,    // Admin activated subscription
    MANUALLY_CANCELLED,    // Admin cancelled subscription
    MANUALLY_RESET,        // Admin reset subscription state
    ERROR_OCCURRED;        // System error during processing
}

