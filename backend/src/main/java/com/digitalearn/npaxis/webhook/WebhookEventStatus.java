package com.digitalearn.npaxis.webhook;

/**
 * Status enum for webhook event processing.
 *
 * State transitions:
 * PENDING → PROCESSING → SUCCESS (happy path)
 * PENDING → PROCESSING → FAILED_RETRYING (retry path)
 * FAILED_RETRYING → PROCESSING → SUCCESS (retry succeeds)
 * FAILED_RETRYING → DEAD_LETTER (max retries exceeded)
 */
public enum WebhookEventStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED_RETRYING,
    DEAD_LETTER
}

