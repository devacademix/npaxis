package com.digitalearn.npaxis.analytics;

/**
 * Enum representing different analytics events tracked across the backend.
 * <p>
 * These events are automatically tracked via AOP-based annotations on controller
 * and service methods. The annotation-driven approach ensures:
 * - Consistency across the codebase
 * - No duplicate tracking
 * - Easy to extend with new event types
 * - Non-intrusive to business logic
 * <p>
 * BEST PRACTICES:
 * - Use descriptive, action-oriented names (VERB_NOUN pattern)
 * - Add constants only for events tracked via @TrackEvent annotation
 * - For complex conditionals, use manual tracking in services
 * - Prefer service-layer tracking over controller-level
 *
 * @author Backend Team
 * @version 1.1
 */
public enum EventType {

    // ============================================
    // Profile Events
    // ============================================

    /**
     * User viewed a preceptor's profile
     * Tracked at: ProfileService.getProfile() or PreceptorController.getById()
     * Metadata: target preceptor ID, view duration (optional)
     */
    PROFILE_VIEWED,

    /**
     * Student viewed list of preceptors
     * Tracked at: PreceptorController.searchPreceptors()
     * Metadata: filter criteria, result count
     */
    PROFILE_LIST_VIEWED,

    /**
     * Student revealed a preceptor's contact information
     * Tracked at: ContactService.revealContact()
     * Metadata: contact type (phone, email, etc.)
     */
    CONTACT_REVEALED,

    /**
     * Student requested to contact a preceptor
     * Tracked at: ContactService.requestContact()
     * Metadata: contact method, reason (if applicable)
     */
    CONTACT_REQUESTED,

    // ============================================
    // Authentication & User Events
    // ============================================

    /**
     * User logged in successfully
     * Tracked at: AuthService.login() or AuthService.refreshToken()
     * Metadata: IP address (via HttpServletRequest), user agent, login method
     */
    USER_LOGIN,

    /**
     * User logged out
     * Tracked at: AuthService.logout()
     * Metadata: session duration
     */
    USER_LOGOUT,

    /**
     * New user registered
     * Tracked at: AuthService.register()
     * Metadata: user type (STUDENT/PRECEPTOR), registration method
     */
    USER_REGISTERED,

    // ============================================
    // Search & Discovery Events
    // ============================================

    /**
     * User performed a search
     * Tracked at: PreceptorController.searchPreceptors()
     * Metadata: search query, filters applied, result count
     */
    SEARCH_PERFORMED,

    /**
     * User applied filters to search results
     * Tracked at: PreceptorController.searchPreceptors()
     * Metadata: filter types and values applied
     */
    FILTER_APPLIED,

    // ============================================
    // Page Navigation Events
    // ============================================

    /**
     * User viewed a specific page
     * Tracked at: Various controllers for page-level tracking (optional)
     * Metadata: page identifier, source page
     */
    PAGE_VIEWED,

    // ============================================
    // Interaction Events
    // ============================================

    /**
     * User clicked on an item or link
     * Tracked at: Service layer for important interactions
     * Metadata: item ID, item type, action performed
     */
    ITEM_CLICKED,

    // ============================================
    // API & System Events
    // ============================================

    /**
     * External API call was made by user
     * Tracked at: Service layer for external integrations
     * Metadata: API endpoint, request type, response status
     */
    API_CALLED,

    // ============================================
    // Inquiry & Support Events
    // ============================================

    /**
     * User submitted an inquiry
     * Tracked at: InquiryService.submitInquiry()
     * Metadata: inquiry type, related preceptor ID
     */
    INQUIRY_SUBMITTED,

    // ============================================
    // Subscription Events (Premium Features)
    // ============================================

    /**
     * User viewed subscription/pricing page
     * Tracked at: SubscriptionController (if exists) or via manual tracking
     * Metadata: plan viewed, previous subscription (if any)
     */
    SUBSCRIPTION_PAGE_VIEWED,

    /**
     * User upgraded their subscription plan
     * Tracked at: SubscriptionService.upgradeSubscription()
     * Metadata: old plan, new plan, new amount
     */
    SUBSCRIPTION_UPGRADED,

    /**
     * User downgraded their subscription plan
     * Tracked at: SubscriptionService.downgradeSubscription()
     * Metadata: old plan, new plan
     */
    SUBSCRIPTION_DOWNGRADED,

    /**
     * User canceled their subscription
     * Tracked at: SubscriptionService.cancelSubscription()
     * Metadata: subscription ID, reason (if provided)
     */
    SUBSCRIPTION_CANCELED,

    /**
     * Subscription payment succeeded
     * Tracked at: SubscriptionService or WebhookService.handlePaymentSuccess()
     * Metadata: amount, payment method, invoice ID
     */
    PAYMENT_SUCCEEDED,

    /**
     * Subscription payment failed
     * Tracked at: SubscriptionService or WebhookService.handlePaymentFailed()
     * Metadata: amount, failure reason
     */
    PAYMENT_FAILED,

    // ============================================
    // Content & Resource Events
    // ============================================

    /**
     * User downloaded a resource (certificate, license, etc.)
     * Tracked at: DocumentService.downloadDocument()
     * Metadata: document type, document ID
     */
    RESOURCE_DOWNLOADED,

    /**
     * User uploaded a resource or file
     * Tracked at: FileService.uploadFile()
     * Metadata: file type, file size
     */
    RESOURCE_UPLOADED,

    // ============================================
    // Engagement Events
    // ============================================

    /**
     * User bookmarked or favorited a preceptor
     * Tracked at: FavoriteService.addFavorite()
     * Metadata: preceptor ID
     */
    PRECEPTOR_FAVORITED,

    /**
     * User removed a preceptor from bookmarks
     * Tracked at: FavoriteService.removeFavorite()
     * Metadata: preceptor ID
     */
    PRECEPTOR_UNFAVORITED,

}

