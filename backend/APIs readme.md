# NPAxis API Documentation

This document provides a comprehensive list of API endpoints available in the NPAxis backend, their functionality, input requirements, and expected outputs.

**Base URL:** `/api/v1`

---

## 1. Authentication (`/auth`)

| Endpoint | Method | Description | Input | Output |
| :--- | :--- | :--- | :--- | :--- |
| `/login` | POST | Authenticates a user. | `AuthRequest` | `AuthResponse` |
| `/register` | POST | Registers a new user (Student or Preceptor). | `BaseRegistrationRequest` | Success message |
| `/refresh-token` | POST | Refreshes the JWT token. | `refreshToken` (Cookie) | `AuthResponse` |
| `/logout` | POST | Logs out the user. | `refreshToken` (Cookie) | Success message |
| `/verify-otp` | POST | Verifies OTP for account activation. | `VerifyOTPRequest` | `AuthResponse` |
| `/forgot-password` | POST | Initiates forgot password flow. | `ForgotPasswordRequest` | Success message |
| `/reset-password` | POST | Resets user password. | `AuthRequest` | Success message |
| `/initialize` | POST | Initializes default roles and admin user. | None | Success message |

### Data Objects

#### `AuthRequest`
| Field | Type | Description |
| :--- | :--- | :--- |
| `email` | `String` | User email address. |
| `password` | `String` | User password. |

#### `AuthResponse`
| Field | Type | Description |
| :--- | :--- | :--- |
| `userId` | `Long` | Unique identifier of the user. |
| `displayName` | `String` | Full name of the user. |
| `email` | `String` | Email address of the user. |
| `accessToken` | `String` | JWT access token. |
| `role` | `String` | Assigned role name. |

#### `BaseRegistrationRequest` (Abstract)
*Polymorphic based on `roleId` (1 = Student, 2 = Preceptor)*
| Field | Type | Description |
| :--- | :--- | :--- |
| `email` | `String` | User email address. |
| `password` | `String` | User password. |
| `displayName` | `String` | Full name of the user. |
| `roleId` | `Long` | Role ID (1: Student, 2: Preceptor). |

#### `StudentRegistrationRequest` (Extends `BaseRegistrationRequest`)
| Field | Type | Description |
| :--- | :--- | :--- |
| `university` | `String` | University name. |
| `program` | `String` | Academic program. |
| `graduationYear` | `String` | Year of graduation. |
| `phone` | `String` | Contact phone number. |

#### `PreceptorRegistrationRequest` (Extends `BaseRegistrationRequest`)
| Field | Type | Description |
| :--- | :--- | :--- |
| `credentials` | `String` | Professional credentials (e.g., NP, MD). |
| `specialty` | `String` | Area of specialty. |
| `location` | `String` | Geographic location. |
| `phone` | `String` | Contact phone number. |

#### `VerifyOTPRequest`
| Field | Type | Description |
| :--- | :--- | :--- |
| `email` | `String` | User email address. |
| `otp` | `String` | One-time password received via email. |

#### `ForgotPasswordRequest`
| Field | Type | Description |
| :--- | :--- | :--- |
| `email` | `String` | User email address. |

---

## 2. User Management (`/users`)

| Endpoint | Method | Description | Input | Output |
| :--- | :--- | :--- | :--- | :--- |
| `/user/me` | GET | Fetches currently logged-in user. | None | `LoggedInUserResponseDTO` |
| `/user-{userId}` | PUT | Updates user information. | `userId` (Path), `UserRequestDTO` | `UserResponseDTO` |
| `/active/all` | GET | Fetches all active users. | None | List of `UserResponseDTO` |
| `/active/user-{userId}` | GET | Fetches an active user by ID. | `userId` (Path) | `UserResponseDTO` |
| `/soft-delete/user-{userId}`| DELETE | Deactivates a user (soft delete). | `userId` (Path) | Success message |
| `/all` | GET | Fetches all users (including deleted). | None | List of `UserResponseDTO` |
| `/deleted/all` | GET | Fetches all soft-deleted users (Admin). | None | List of `UserResponseDTO` |
| `/deleted/user-{userId}` | GET | Fetches a soft-deleted user (Admin). | `userId` (Path) | `UserResponseDTO` |
| `/restore/user-{userId}` | PUT | Restores a soft-deleted user (Admin). | `userId` (Path) | Success message |
| `/hard-delete/user-{userId}`| DELETE | Permanently deletes a user (Admin). | `userId` (Path) | Success message |

### Data Objects

#### `LoggedInUserResponseDTO`
| Field | Type | Description |
| :--- | :--- | :--- |
| `userId` | `Long` | Unique identifier. |
| `username` | `String` | User email/username. |
| `name` | `String` | Display name. |
| `email` | `String` | Email address. |

#### `UserRequestDTO`
| Field | Type | Description |
| :--- | :--- | :--- |
| `fullName` | `String` | Full name. |
| `username` | `String` | Email/Username. |
| `password` | `String` | New password. |
| `email` | `String` | Email address. |
| `roles` | `Set<Long>` | Set of role IDs. |

#### `UserResponseDTO`
| Field | Type | Description |
| :--- | :--- | :--- |
| `userId` | `Long` | Unique identifier. |
| `displayName` | `String` | Display name. |
| `email` | `String` | Email address. |
| `role` | `String` | Role name. |

---

## 3. Preceptor Management (`/preceptors`)

| Endpoint | Method | Description | Input | Output |
| :--- | :--- | :--- | :--- | :--- |
| `/search` | GET | Search and filter preceptors. | Query params | Paginated list of `PreceptorResponseDTO` |
| `/active/preceptor-{userId}`| GET | Fetches preceptor by ID. | `userId` (Path) | `PreceptorResponseDTO` |
| `/preceptor-{userId}` | PUT | Updates preceptor details. | `userId` (Path), `PreceptorRequestDTO`| `PreceptorResponseDTO` |
| `/soft-delete/preceptor-{userId}`| DELETE| Deactivates a preceptor. | `userId` (Path) | Success message |
| `/hard-delete/preceptor-{userId}`| DELETE| Permanently deletes a preceptor (Admin).| `userId` (Path) | Success message |
| `/restore/preceptor-{userId}`| PUT | Restores a soft-deleted preceptor. | `userId` (Path) | Success message |
| `/verify/preceptor-{userId}`| PUT | Verifies a preceptor (Admin). | `userId` (Path) | `PreceptorResponseDTO` |
| `/preceptor-{userId}/submit-license`| POST | Submits license for verification. | `userId` (Path), `PreceptorRequestDTO`| `PreceptorResponseDTO` |
| `/preceptor-{userId}/reveal-contact`| POST | Reveals preceptor contact (Premium).| `userId` (Path) | `PreceptorContactResponseDTO` |

### Data Objects

#### `PreceptorRequestDTO`
| Field | Type | Description |
| :--- | :--- | :--- |
| `name` | `String` | Preceptor full name. |
| `credentials` | `String` | Professional credentials. |
| `specialty` | `String` | Specialty area. |
| `location` | `String` | Work location. |
| `setting` | `String` | Clinical setting. |
| `availableDays` | `List<DayOfWeek>`| List of available days (e.g., MONDAY). |
| `honorarium` | `String` | Honorarium details. |
| `requirements` | `String` | Preceptorship requirements. |
| `email` | `String` | Contact email. |
| `phone` | `String` | Contact phone. |
| `licenseNumber` | `String` | Professional license number. |
| `licenseState` | `String` | State of licensure. |
| `licenseFileUrl` | `String` | URL to uploaded license document. |

#### `PreceptorResponseDTO`
| Field | Type | Description |
| :--- | :--- | :--- |
| `userId` | `Long` | Unique identifier. |
| `displayName` | `String` | Display name. |
| `credentials` | `String` | Credentials. |
| `specialty` | `String` | Specialty. |
| `location` | `String` | Location. |
| `setting` | `String` | Clinical setting. |
| `availableDays` | `List<Day>` | Days available. |
| `honorarium` | `String` | Honorarium. |
| `requirements` | `String` | Requirements text. |
| `isVerified` | `boolean` | Verification status. |
| `isPremium` | `boolean` | Premium status. |
| `licenseNumber` | `String` | License number. |
| `licenseState` | `String` | License state. |
| `licenseFileUrl` | `String` | License file link. |
| `verificationStatus`| `String` | Status (PENDING, APPROVED, REJECTED).|
| `verificationSubmittedAt`| `DateTime`| When verification was submitted. |
| `verificationReviewedAt` | `DateTime`| When verification was reviewed. |

#### `PreceptorContactResponseDTO`
| Field | Type | Description |
| :--- | :--- | :--- |
| `phone` | `String` | Preceptor contact phone number. |
| `email` | `String` | Preceptor contact email address. |

---

## 4. Student Management (`/students`)

| Endpoint | Method | Description | Input | Output |
| :--- | :--- | :--- | :--- | :--- |
| `/` | GET | Search and filter students. | Query params | Paginated list of `StudentResponseDTO` |
| `/active/all` | GET | Fetches all active students. | None | List of `StudentResponseDTO` |
| `/active/student-{userId}`| GET | Fetches student by ID. | `userId` (Path) | `StudentResponseDTO` |
| `/student-{userId}` | PUT | Updates student details. | `userId` (Path), `StudentRequestDTO` | `StudentResponseDTO` |
| `/soft-delete/student-{userId}`| DELETE | Deactivates a student. | `userId` (Path) | Success message |
| `/hard-delete/student-{userId}`| DELETE | Permanently deletes a student (Admin). | `userId` (Path) | Success message |
| `/restore/student-{userId}`| PUT | Restores a soft-deleted student. | `userId` (Path) | Success message |
| `/student-{userId}/save-preceptor/{preceptorId}`| POST | Bookmarks a preceptor. | `userId`, `preceptorId` (Path) | Success message |
| `/student-{userId}/saved` | GET | Fetches bookmarked preceptors. | `userId` (Path) | List of `PreceptorResponseDTO` |

### Data Objects

#### `StudentRequestDTO`
| Field | Type | Description |
| :--- | :--- | :--- |
| `university` | `String` | University name. |
| `program` | `String` | Academic program. |
| `graduationYear` | `String` | Year of graduation. |
| `phone` | `String` | Contact phone. |

#### `StudentResponseDTO`
| Field | Type | Description |
| :--- | :--- | :--- |
| `userId` | `Long` | Unique identifier. |
| `displayName` | `String` | Display name. |
| `email` | `String` | Email address. |
| `university` | `String` | University. |
| `program` | `String` | Program. |
| `graduationYear` | `String` | Graduation year. |
| `phone` | `String` | Phone number. |

---

## 5. Administration (`/administration`)

| Endpoint | Method | Description | Input | Output |
| :--- | :--- | :--- | :--- | :--- |
| `/add-admin` | POST | Registers a new admin user. | `AdminRegisterRequest` | `AdminRegisterResponse` |
| `/preceptors/pending` | GET | Fetches pending preceptor requests. | Pageable params | Paginated list of `Preceptor` (Entity) |
| `/preceptors/approve-{userId}`| POST | Approves a preceptor request. | `userId` (Path) | Success message |
| `/preceptors/reject-{userId}`| POST | Rejects a preceptor request. | `userId` (Path) | Success message |
| `/all-admins` | GET | Fetches all admin users. | None | List of `User` (Entity) |
| `/user-{userId}/toggle-account`| PUT | Enables/disables a user account. | `userId` (Path), `enabled` (Query) | Success message |

### Data Objects

#### `AdminRegisterRequest`
| Field | Type | Description |
| :--- | :--- | :--- |
| `email` | `String` | Admin email address. |
| `password` | `String` | Admin password. |
| `displayName` | `String` | Admin display name. |

#### `AdminRegisterResponse`
| Field | Type | Description |
| :--- | :--- | :--- |
| `userId` | `Long` | Unique identifier. |
| `displayName` | `String` | Display name. |
| `email` | `String` | Email address. |
| `role` | `String` | Assigned role. |

---

## 6. Analytics (`/analytics`)

| Endpoint | Method | Description | Input | Output |
| :--- | :--- | :--- | :--- | :--- |
| `/event` | POST | Logs an analytics event. | `AnalyticsEventRequest` | None |
| `/preceptors/{id}/stats` | GET | Fetches statistics for a preceptor. | `id` (Path) | `PreceptorStatsResponse` |

### Data Objects

#### `AnalyticsEventRequest`
| Field | Type | Description |
| :--- | :--- | :--- |
| `eventType` | `String` | Type of event (e.g., PROFILE_VIEW, CONTACT_REVEAL). |
| `preceptorId` | `Long` | Target preceptor ID. |

#### `PreceptorStatsResponse`
| Field | Type | Description |
| :--- | :--- | :--- |
| `profileViews` | `Long` | Total profile views. |
| `contactReveals` | `Long` | Total contact reveals. |
| `inquiries` | `Long` | Total inquiries. |

---

## 7. Role Management (`/roles`)

| Endpoint | Method | Description | Input | Output |
| :--- | :--- | :--- | :--- | :--- |
| `/active/all` | GET | Fetches all active roles. | None | List of `RoleResponseDTO` |
| `/active/role-{roleId}` | GET | Fetches a role by ID. | `roleId` (Path) | `RoleResponseDTO` |

### Data Objects

#### `RoleResponseDTO`
| Field | Type | Description |
| :--- | :--- | :--- |
| `roleId` | `Long` | Unique role identifier. |
| `roleName` | `String` | Name of the role (STUDENT, PRECEPTOR, ADMIN). |
| `description` | `String` | Brief description of the role. |
