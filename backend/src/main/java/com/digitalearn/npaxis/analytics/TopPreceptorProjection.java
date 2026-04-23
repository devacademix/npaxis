package com.digitalearn.npaxis.analytics;

/**
 * Projection for top preceptors.
 */
public interface TopPreceptorProjection {

    Long getPreceptorId();

    String getDisplayName();

    Long getInquiryCount();
}