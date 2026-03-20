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
    public static final String ACTIVATE_ACCOUNT = "activate-account";

    /**
     * API endpoint for refreshing the JWT token.
     * This is typically used to obtain a new access token using a valid refresh token.
     */
    public static final String REFRESH_TOKEN = "refresh-token";

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
    public static final String PUT_UPDATE_PRECEPTOR_API = "preceptor-{userId}";
    public static final String GET_ALL_ACTIVE_PRECEPTORS_API = "active/all";
    public static final String GET_ACTIVE_PRECEPTOR_BY_ID_API = "active/preceptor-{userId}";
    public static final String SOFT_DELETE_PRECEPTOR_BY_ID_API = "soft-delete/preceptor-{userId}";
    public static final String HARD_DELETE_PRECEPTOR_BY_ID_API = "hard-delete/preceptor-{userId}";
    public static final String RESTORE_PRECEPTOR_BY_ID_API = "restore/preceptor-{userId}";
    public static final String VERIFY_PRECEPTOR_API = "verify/preceptor-{userId}";
    public static final String SUBMIT_LICENSE_API = "preceptor-{userId}/submit-license";
    public static final String REVEAL_CONTACT_API = "preceptor-{userId}/reveal-contact";

    // APIs for Admin Management
    public static final String ADMINISTRATION_API = "administration";
    public static final String ADD_ADMIN_API = "/add-admin";
    public static final String GET_PENDING_PRECEPTORS_API = "/preceptors/pending";
    public static final String APPROVE_PRECEPTOR_API = "/preceptors/approve-{userId}";
    public static final String REJECT_PRECEPTOR_API = "/preceptors/reject-{userId}";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private APIConstants() {
        // Prevent instantiation
    }


}
