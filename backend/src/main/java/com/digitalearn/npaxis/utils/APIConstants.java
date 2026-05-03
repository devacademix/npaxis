package com.digitalearn.npaxis.utils;

/**
 * This class contains constants used for API endpoints in the application.
 * It includes base API paths and specific endpoints for authentication and HL7 operations.
 * <p>
 * The constants are organized into categories for better readability and maintainability.
 * </p>
 */
public class APIConstants {

    //    Base API
    /**
     * Base API path for the application.
     */
    public static final String BASE_API = "api/v1";
    //    Auth APIs
    /**
     * Auth APIs.
     */
    public static final String AUTH_API = "auth";
    /**
     * API endpoint for user login.
     */
    public static final String LOGIN_API = "login";
    /**
     * API endpoint for user logout.
     */
    public static final String LOGOUT_API = "logout";

    public static final String VERIFY_OTP_API = "verify-otp";

    public static final String FORGOT_PASSWORD_API = "forgot-password";

    public static final String RESET_PASSWORD_API = "reset-password";

    /**
     * API endpoint for refreshing the JWT token.
     * This is typically used to obtain a new access token using a valid refresh token.
     */
    public static final String REFRESH_TOKEN_API = "refresh-token";

    /**
     * API endpoint for admin initialization of roles and users.
     * This is typically used to set up the system with default roles and users.
     */
    public static final String INITIALIZE_ROLE_AND_USER_API = "initialize";

//    APIs for Role Management
    /**
     * Base API endpoint for role management.
     */
    public static final String ROLES_API = "roles";

    public static final String GET_ALL_ACTIVE_ROLES_API = "active/all";

    public static final String GET_ACTIVE_ROLE_BY_ID_API = "active/role-{roleId}";

    //    APIs for User Management
    /**
     * Base API endpoint for user management.
     */
    public static final String USERS_API = "users";

    /**
     * API endpoint for fetching the currently logged-in user.
     */
    public static final String GET_CURRENTLY_LOGGED_IN_USER_API = "user/me";

    /**
     * API endpoint for new user registration.
     */
    public static final String USER_REGISTRATION_API = "register";

    /**
     * API endpoint for PUT updating user information.
     */
    public static final String PUT_UPDATE_USER_API = "user-{userId}";

    /**
     * API endpoint for fetching all users.
     */
    public static final String GET_ALL_ACTIVE_USERS_API = "active/all";

    /**
     * API endpoint for fetching a user by their ID.
     */
    public static final String GET_ACTIVE_USER_BY_ID_API = "active/user-{userId}";

    /**
     * API endpoint for removing a user.
     */
    public static final String SOFT_DELETE_USER_BY_ID_API = "soft-delete/user-{userId}";

    /**
     * API endpoint for hard deleting a user by their ID.
     */
    public static final String HARD_DELETE_USER_BY_ID_API = "hard-delete/user-{userId}";

    public static final String UPLOAD_PROFILE_PICTURE_API = "user-{userId}/upload-profile-picture";
    public static final String DOWNLOAD_PROFILE_PICTURE_API = "user-{userId}/profile-picture";

    /**
     * API endpoint for fetching all users including soft-deleted ones.
     */
    public static final String GET_ALL_USERS_API = "all";

    /**
     * API endpoint for fetching all soft-deleted users.
     */
    public static final String GET_ALL_SOFT_DELETED_USERS_API = "deleted/all";

    /**
     * API endpoint for fetching a soft-deleted user by their ID.
     */
    public static final String GET_SOFT_DELETED_USER_BY_ID_API = "deleted/user-{userId}";

    /**
     * API endpoint for restoring a soft-deleted user by their ID.
     */
    public static final String RESTORE_USER_BY_ID_API = "restore/user-{userId}";

//     User Access Roles APIs

    /**
     * API endpoint for removing a user access role.
     */
    public static final String REMOVE_USER_ACCESS_ROLE_API = "user-{userId}/remove-roles";

//      APIs for Party Management
    /**
     * Base API endpoint for party management.
     */
    public static final String PARTIES_API = "parties";

    /**
     * API endpoint for adding a new party.
     */
    public static final String PARTY_REGISTRATION_API = "register";
    /**
     * API endpoint for PUT updating party information.
     */
    public static final String PUT_UPDATE_PARTY_API = "party-{partyId}";
    /**
     * API endpoint for fetching all parties.
     */
    public static final String GET_ALL_ACTIVE_PARTIES_API = "active/all";
    /**
     * API endpoint for fetching a party by their ID.
     */
    public static final String GET_ACTIVE_PARTY_BY_ID_API = "active/party-{partyId}";

    /**
     * API endpoint for soft-removing a party.
     */
    public static final String SOFT_DELETE_PARTY_BY_ID_API = "soft-delete/party-{partyId}";

    /**
     * API endpoint for hard-removing a party.
     */
    public static final String HARD_DELETE_PARTY_BY_ID_API = "hard-delete/party-{partyId}";

    /**
     * API endpoint for fetching all users including soft-deleted ones.
     */
    public static final String GET_ALL_PARTIES_API = "all";

    /**
     * API endpoint for fetching all soft-deleted parties.
     */
    public static final String GET_ALL_SOFT_DELETED_PARTIES_API = "deleted/all";

    /**
     * API endpoint for fetching a soft-deleted party by their ID.
     */
    public static final String GET_SOFT_DELETED_PARTY_BY_ID_API = "deleted/party-{partyId}";

    /**
     * API endpoint for restoring a soft-deleted party by their ID.
     */
    public static final String RESTORE_PARTY_BY_ID_API = "restore/party-{partyId}";


    //      APIs for Person Names Management
    /**
     * Base API endpoint for person name management.
     */
    public static final String PERSON_NAMES_API = "person-names";

    /**
     * API endpoint for adding a new person name.
     */
    public static final String PERSON_NAME_REGISTRATION_API = "register";

    /**
     * API endpoint for PUT updating person name information.
     */
    public static final String PUT_UPDATE_PERSON_NAME_API = "person-name-{personNameId}";

    /**
     * API endpoint for fetching all active person names.
     */
    public static final String GET_ALL_ACTIVE_PERSON_NAMES_API = "active/all";

    /**
     * API endpoint for fetching an active person name by their ID.
     */
    public static final String GET_ACTIVE_PERSON_NAME_BY_ID_API = "active/person-name-{personNameId}";

    /**
     * API endpoint for soft-deleting a person name by their ID.
     */
    public static final String SOFT_DELETE_PERSON_NAME_BY_ID_API = "soft-delete/person-name-{personNameId}";

    /**
     * API endpoint for hard-deleting a person name by their ID.
     */
    public static final String HARD_DELETE_PERSON_NAME_BY_ID_API = "hard-delete/person-name-{personNameId}";

    /**
     * API endpoint for fetching all person names including soft-deleted ones.
     */
    public static final String GET_ALL_PERSON_NAMES_API = "all";

    /**
     * API endpoint for fetching all soft-deleted person names.
     */
    public static final String GET_ALL_SOFT_DELETED_PERSON_NAMES_API = "deleted/all";

    /**
     * API endpoint for fetching a soft-deleted person name by their ID.
     */
    public static final String GET_SOFT_DELETED_PERSON_NAME_BY_ID_API = "deleted/person-name-{personNameId}";

    /**
     * API endpoint for restoring a soft-deleted person name by their ID.
     */
    public static final String RESTORE_PERSON_NAME_BY_ID_API = "restore/person-name-{personNameId}";

    //    Person Name operations with Party
    public static final String GET_ALL_PERSON_NAMES_FOR_PARTY_API = "party-{partyId}/person-names";

//          APIs for Patient Management
    /**
     * Base API endpoint for patient management.
     */
    public static final String PATIENTS_API = "patients";

    /**
     * API endpoint for adding a new patient.
     */
    public static final String PATIENT_REGISTRATION_API = "register";

    /**
     * API endpoint for PUT updating patient information.
     */
    public static final String PUT_UPDATE_PATIENT_API = "patient-{patientId}";

    /**
     * API endpoint for fetching all active patients.
     */
    public static final String GET_ALL_ACTIVE_PATIENTS_API = "active/all";

    /**
     * API endpoint for fetching a patient by their ID.
     */
    public static final String GET_ACTIVE_PATIENT_BY_ID_API = "active/patient-{patientId}";

    /**
     * API endpoint for soft-removing a patient.
     */
    public static final String SOFT_DELETE_PATIENT_BY_ID_API = "soft-delete/patient-{patientId}";

    /**
     * API endpoint for hard-removing a patient.
     */
    public static final String HARD_DELETE_PATIENT_BY_ID_API = "hard-delete/patient-{patientId}";

    /**
     * API endpoint for fetching all patients including soft-deleted ones.
     */
    public static final String GET_ALL_PATIENTS_API = "all";

    /**
     * API endpoint for fetching all soft-deleted patients.
     */
    public static final String GET_ALL_SOFT_DELETED_PATIENTS_API = "deleted/all";

    /**
     * API endpoint for fetching a soft-deleted patient by their ID.
     */
    public static final String GET_SOFT_DELETED_PATIENT_BY_ID_API = "deleted/patient-{patientId}";

    /**
     * API endpoint for restoring a soft-deleted patient by their ID.
     */
    public static final String RESTORE_PATIENT_BY_ID_API = "restore/patient-{patientId}";

    // APIs for Student Management
    public static final String STUDENTS_API = "students";
    public static final String PUT_UPDATE_STUDENT_API = "student-{userId}";
    public static final String GET_ALL_ACTIVE_STUDENTS_API = "active/all";
    public static final String GET_ACTIVE_STUDENT_BY_ID_API = "active/student-{userId}";
    public static final String SOFT_DELETE_STUDENT_BY_ID_API = "soft-delete/student-{userId}";
    public static final String HARD_DELETE_STUDENT_BY_ID_API = "hard-delete/student-{userId}";
    public static final String RESTORE_STUDENT_BY_ID_API = "restore/student-{userId}";
    public static final String SAVE_PRECEPTOR_API = "student-{userId}/save-preceptor/{preceptorId}";
    public static final String GET_SAVED_PRECEPTORS_API = "student-{userId}/saved";

    // APIs for Preceptor Management
    public static final String PRECEPTORS_API = "preceptors";
    public static final String PRECEPTORS_SEARCH_API = "search";
    public static final String PUT_UPDATE_PRECEPTOR_API = "preceptor-{userId}";
    public static final String GET_ALL_ACTIVE_PRECEPTORS_API = "active/all";
    public static final String GET_ACTIVE_PRECEPTOR_BY_ID_API = "active/preceptor-{userId}";
    public static final String SOFT_DELETE_PRECEPTOR_BY_ID_API = "soft-delete/preceptor-{userId}";
    public static final String HARD_DELETE_PRECEPTOR_BY_ID_API = "hard-delete/preceptor-{userId}";
    public static final String RESTORE_PRECEPTOR_BY_ID_API = "restore/preceptor-{userId}";
    public static final String VERIFY_PRECEPTOR_API = "verify/preceptor-{userId}";
    public static final String SUBMIT_LICENSE_API = "preceptor-{userId}/submit-license";
    public static final String DOWNLOAD_LICENSE_API = "preceptor-{userId}/license";
    public static final String REVEAL_CONTACT_API = "active/preceptor-{userId}/reveal-contact";

    // APIs for Inquiry Management
    public static final String INQUIRIES_BASE_API = "inquiries";
    public static final String SEND_INQUIRY_API = "send";
    public static final String GET_INQUIRIES_FOR_USER = "my-inquiries";
    public static final String MARK_INQUIRY_AS_READ = "/{inquiryId}/read";

    // APIs for Admin Management
    public static final String ADMINISTRATION_API = "administration";
    public static final String ADD_ADMIN_API = "/add-admin";
    public static final String GET_PENDING_PRECEPTORS_API = "/preceptors/pending";
    public static final String APPROVE_PRECEPTOR_API = "/preceptors/approve-{userId}";
    public static final String REJECT_PRECEPTOR_API = "/preceptors/reject-{userId}";

    // APIs for Admin Preceptor Management
    public static final String ADMIN_PRECEPTORS_LIST_API = "/preceptors/list";
    public static final String ADMIN_PRECEPTOR_DETAIL_API = "/preceptors/detail-{userId}";
    public static final String ADMIN_PRECEPTOR_UPDATE_API = "/preceptors/update-{userId}";
    public static final String ADMIN_PRECEPTOR_LICENSE_REVIEW_API = "/preceptors/{userId}/license/review";
    public static final String ADMIN_PRECEPTOR_LICENSE_APPROVE_API = "/preceptors/{userId}/license/approve";
    public static final String ADMIN_PRECEPTOR_LICENSE_REJECT_API = "/preceptors/{userId}/license/reject";
    public static final String ADMIN_PRECEPTOR_LICENSE_DOWNLOAD_API = "/preceptors/{userId}/license/download";
    public static final String ADMIN_PRECEPTOR_CONTACT_REVEAL_API = "/preceptors/{userId}/contact";
    public static final String ADMIN_PRECEPTOR_VERIFICATION_HISTORY_API = "/preceptors/{userId}/verification-history";
    public static final String ADMIN_PRECEPTOR_VERIFICATION_NOTES_API = "/preceptors/{userId}/verification-notes";
    public static final String ADMIN_PRECEPTOR_BILLING_REPORT_API = "/preceptors/{userId}/billing";
    public static final String ADMIN_PRECEPTOR_ANALYTICS_API = "/preceptors/{userId}/analytics";

    // APIs for Admin Preceptor Verification Lists
    public static final String ADMIN_PRECEPTORS_APPROVED_API = "/preceptors/verified/approved";
    public static final String ADMIN_PRECEPTORS_REJECTED_API = "/preceptors/verified/rejected";
    public static final String ADMIN_PRECEPTORS_PENDING_API = "/preceptors/verified/pending";

    // APIs for Admin Dashboard & Settings
    public static final String ADMIN_DASHBOARD_API = "/dashboard";
    public static final String ADMIN_SETTINGS_API = "/settings";
    public static final String ADMIN_SETTINGS_GENERAL_API = "/settings/general";
    public static final String ADMIN_SETTINGS_INTEGRATIONS_API = "/settings/integrations";
    public static final String ADMIN_SETTINGS_NOTIFICATIONS_API = "/settings/notifications";
    public static final String ADMIN_SETTINGS_SYSTEM_CONTROLS_API = "/settings/system-controls";

    // APIs for Admin Revenue & Reporting
    public static final String ADMIN_REVENUE_API = "/revenue";
    public static final String ADMIN_REVENUE_SUMMARY_API = "/revenue/summary";
    public static final String ADMIN_REVENUE_TRANSACTION_HISTORY_API = "/revenue/transactions";
    public static final String ADMIN_REVENUE_BY_PRECEPTOR_API = "/revenue/by-preceptor";
    public static final String ADMIN_INVOICES_API = "/revenue/invoices";
    public static final String ADMIN_SUBSCRIPTIONS_API = "/revenue/subscriptions";

    // APIs for Admin Student Management
    public static final String ADMIN_STUDENTS_LIST_API = "/students/list";
    public static final String ADMIN_STUDENT_DETAIL_API = "/students/detail-{userId}";
    public static final String ADMIN_STUDENT_UPDATE_API = "/students/update-{userId}";
    public static final String ADMIN_STUDENTS_SEARCH_API = "/students/search";

    // APIs for Admin Role Management
    public static final String ADMIN_ROLES_CREATE_API = "/roles";
    public static final String ADMIN_ROLES_UPDATE_API = "/roles/role-{roleId}";
    public static final String ADMIN_ROLES_DELETE_API = "/roles/role-{roleId}";
    public static final String ADMIN_USER_ROLES_ASSIGN_API = "/users/user-{userId}/roles";
    public static final String ADMIN_USER_ROLES_REMOVE_API = "/users/user-{userId}/roles/remove";

    // APIs for Admin Webhook Management
    public static final String ADMIN_WEBHOOKS_LIST_API = "/webhooks";
    public static final String ADMIN_WEBHOOK_DETAIL_API = "/webhooks/event-{eventId}";
    public static final String ADMIN_WEBHOOK_RETRY_API = "/webhooks/event-{eventId}/retry";
    public static final String ADMIN_WEBHOOK_HISTORY_API = "/webhooks/history";
    public static final String ADMIN_WEBHOOK_METRICS_API = "/webhooks/metrics";

    // APIs for Admin Analytics
    public static final String ADMIN_ANALYTICS_OVERVIEW_API = "/analytics/overview";
    public static final String ADMIN_ANALYTICS_TOP_PRECEPTORS_API = "/analytics/top-preceptors";
    public static final String ADMIN_ANALYTICS_TRENDS_API = "/analytics/trends";

    // APIs for Subscription Management
    public static final String SUBSCRIPTIONS_API = "subscriptions";
    public static final String CHECKOUT_API = "checkout";
    public static final String SUBSCRIPTION_STATUS_API = "status";
    public static final String CANCEL_SUBSCRIPTION_API = "cancel";
    public static final String UPDATE_SUBSCRIPTION_API = "update";
    public static final String SUBSCRIPTION_HISTORY_API = "history";
    public static final String BILLING_PORTAL_API = "billing-portal";
    public static final String ACCESS_CHECK_API = "access-check";

    // APIs for Payment Management
    public static final String PAYMENTS_API = "payments";
    public static final String CREATE_CHECKOUT_SESSION_API = "create-checkout-session";

    // APIs for Subscription Plans
    public static final String PLANS_API = "subscription-plans";

    // APIs for Analytics
    public static final String ANALYTICS_API = "analytics";
    public static final String ANALYTICS_EVENT_API = "event";
    public static final String PRECEPTOR_STATS_API = "preceptors/{id}/stats";

    // APIs for Webhooks
    public static final String WEBHOOKS_API = "webhooks";
    public static final String WEBHOOK_EVENTS_API = "events";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private APIConstants() {
        // Prevent instantiation
    }
}
