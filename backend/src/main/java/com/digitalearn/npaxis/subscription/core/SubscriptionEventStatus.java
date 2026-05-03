package com.digitalearn.npaxis.subscription.core;

/**
 * Status of an event record (for processing state)
 */
public enum SubscriptionEventStatus {
    PENDING,   // Event created but not processed
    SUCCESS,   // Event processed successfully
    FAILED     // Event processing failed
}

