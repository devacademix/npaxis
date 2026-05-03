# NPAxis API Documentation

This document provides a comprehensive list of API endpoints available in the NPAxis backend, their functionality, input
requirements, and expected outputs.

**Base URL:** `/api/v1`

---

## 1. Authentication (`/auth`)

| Endpoint           | Method | Description                                  | Input                     | Output          |
|:-------------------|:-------|:---------------------------------------------|:--------------------------|:----------------|
| `/login`           | POST   | Authenticates a user.                        | `AuthRequest`             | `AuthResponse`  |
| `/register`        | POST   | Registers a new user (Student or Preceptor). | `BaseRegistrationRequest` | Success message |
| `/refresh-token`   | POST   | Refreshes the JWT token.                     | `refreshToken` (Cookie)   | `AuthResponse`  |
| `/logout`          | POST   | Logs out the user.                           | `refreshToken` (Cookie)   | Success message |
| `/verify-otp`      | POST   | Verifies OTP for account activation.         | `VerifyOTPRequest`        | `AuthResponse`  |
| `/forgot-password` | POST   | Initiates forgot password flow.              | `ForgotPasswordRequest`   | Success message |
| `/reset-password`  | POST   | Resets user password.                        | `AuthRequest`             | Success message |
| `/initialize`      | POST   | Initializes default roles and admin user.    | None                      | Success message |

### Data Objects

#### `AuthRequest`

| Field      | Type     | Description         |
|:-----------|:---------|:--------------------|
| `email`    | `String` | User email address. |
| `password` | `String` | User password.      |

#### `AuthResponse`

| Field         | Type     | Description                    |
|:--------------|:---------|:-------------------------------|
| `userId`      | `Long`   | Unique identifier of the user. |
| `displayName` | `String` | Full name of the user.         |
| `email`       | `String` | Email address of the user.     |
| `accessToken` | `String` | JWT access token.              |
| `role`        | `String` | Assigned role name.            |

#### `BaseRegistrationRequest` (Abstract)

*Polymorphic based on `roleId` (1 = Student, 2 = Preceptor)*
| Field | Type | Description |
| :--- | :--- | :--- |
| `email` | `String` | User email address. |
| `password` | `String` | User password. |
| `displayName` | `String` | Full name of the user. |
| `roleId` | `Long` | Role ID (1: Student, 2: Preceptor). |

#### `StudentRegistrationRequest` (Extends `BaseRegistrationRequest`)

| Field            | Type     | Description           |
|:-----------------|:---------|:----------------------|
| `university`     | `String` | University name.      |
| `program`        | `String` | Academic program.     |
| `graduationYear` | `String` | Year of graduation.   |
| `phone`          | `String` | Contact phone number. |

#### `PreceptorRegistrationRequest` (Extends `BaseRegistrationRequest`)

| Field         | Type     | Description                              |
|:--------------|:---------|:-----------------------------------------|
| `credentials` | `String` | Professional credentials (e.g., NP, MD). |
| `specialty`   | `String` | Area of specialty.                       |
| `location`    | `String` | Geographic location.                     |
| `phone`       | `String` | Contact phone number.                    |

#### `VerifyOTPRequest`

| Field   | Type     | Description                           |
|:--------|:---------|:--------------------------------------|
| `email` | `String` | User email address.                   |
| `otp`   | `String` | One-time password received via email. |

#### `ForgotPasswordRequest`

| Field   | Type     | Description         |
|:--------|:---------|:--------------------|
| `email` | `String` | User email address. |

---

## 2. User Management (`/users`)

| Endpoint                     | Method | Description                             | Input                             | Output                    |
|:-----------------------------|:-------|:----------------------------------------|:----------------------------------|:--------------------------|
| `/`                          | GET    | Fetches all active users.               | None                              | List of `UserResponseDTO` |
| `/user/me`                   | GET    | Fetches currently logged-in user.       | None                              | `LoggedInUserResponseDTO` |
| `/user-{userId}`             | PUT    | Updates user information.               | `userId` (Path), `UserRequestDTO` | `UserResponseDTO`         |
| `/active/all`                | GET    | Fetches all active users.               | None                              | List of `UserResponseDTO` |
| `/active/user-{userId}`      | GET    | Fetches an active user by ID.           | `userId` (Path)                   | `UserResponseDTO`         |
| `/soft-delete/user-{userId}` | DELETE | Deactivates a user (soft delete).       | `userId` (Path)                   | Success message           |
| `/all`                       | GET    | Fetches all users (including deleted).  | None                              | List of `UserResponseDTO` |
| `/deleted/all`               | GET    | Fetches all soft-deleted users (Admin). | None                              | List of `UserResponseDTO` |
| `/deleted/user-{userId}`     | GET    | Fetches a soft-deleted user (Admin).    | `userId` (Path)                   | `UserResponseDTO`         |
| `/restore/user-{userId}`     | PUT    | Restores a soft-deleted user (Admin).   | `userId` (Path)                   | Success message           |
| `/hard-delete/user-{userId}` | DELETE | Permanently deletes a user (Admin).     | `userId` (Path)                   | Success message           |

### Data Objects

#### `LoggedInUserResponseDTO`

| Field      | Type     | Description          |
|:-----------|:---------|:---------------------|
| `userId`   | `Long`   | Unique identifier.   |
| `username` | `String` | User email/username. |
| `name`     | `String` | Display name.        |
| `email`    | `String` | Email address.       |

#### `UserRequestDTO`

| Field      | Type        | Description      |
|:-----------|:------------|:-----------------|
| `fullName` | `String`    | Full name.       |
| `username` | `String`    | Email/Username.  |
| `password` | `String`    | New password.    |
| `email`    | `String`    | Email address.   |
| `roles`    | `Set<Long>` | Set of role IDs. |

#### `UserResponseDTO`

| Field         | Type     | Description        |
|:--------------|:---------|:-------------------|
| `userId`      | `Long`   | Unique identifier. |
| `displayName` | `String` | Display name.      |
| `email`       | `String` | Email address.     |
| `role`        | `String` | Role name.         |

---

## 3. Preceptor Management (`/preceptors`)

| Endpoint                                    | Method | Description                              | Input                                  | Output                                   |
|:--------------------------------------------|:-------|:-----------------------------------------|:---------------------------------------|:-----------------------------------------|
| `/search`                                   | GET    | Search and filter preceptors.            | Query params                           | Paginated list of `PreceptorResponseDTO` |
| `/active/preceptor-{userId}`                | GET    | Fetches preceptor by ID.                 | `userId` (Path)                        | `PreceptorResponseDTO`                   |
| `/preceptor-{userId}`                       | PUT    | Updates preceptor details.               | `userId` (Path), `PreceptorRequestDTO` | `PreceptorResponseDTO`                   |
| `/soft-delete/preceptor-{userId}`           | DELETE | Deactivates a preceptor.                 | `userId` (Path)                        | Success message                          |
| `/hard-delete/preceptor-{userId}`           | DELETE | Permanently deletes a preceptor (Admin). | `userId` (Path)                        | Success message                          |
| `/restore/preceptor-{userId}`               | PUT    | Restores a soft-deleted preceptor.       | `userId` (Path)                        | Success message                          |
| `/verify/preceptor-{userId}`                | PUT    | Verifies a preceptor (Admin).            | `userId` (Path)                        | `PreceptorResponseDTO`                   |
| `/preceptor-{userId}/submit-license`        | POST   | Submits license for verification.        | `userId` (Path), `PreceptorRequestDTO` | `PreceptorResponseDTO`                   |
| `/active/preceptor-{userId}/reveal-contact` | GET    | Reveals preceptor contact (Premium).     | `userId` (Path)                        | `PreceptorContactResponseDTO`            |

### Data Objects

#### `PreceptorRequestDTO`

| Field            | Type              | Description                            |
|:-----------------|:------------------|:---------------------------------------|
| `name`           | `String`          | Preceptor full name.                   |
| `credentials`    | `String`          | Professional credentials.              |
| `specialty`      | `String`          | Specialty area.                        |
| `location`       | `String`          | Work location.                         |
| `setting`        | `String`          | Clinical setting.                      |
| `availableDays`  | `List<DayOfWeek>` | List of available days (e.g., MONDAY). |
| `honorarium`     | `String`          | Honorarium details.                    |
| `requirements`   | `String`          | Preceptorship requirements.            |
| `email`          | `String`          | Contact email.                         |
| `phone`          | `String`          | Contact phone.                         |
| `licenseNumber`  | `String`          | Professional license number.           |
| `licenseState`   | `String`          | State of licensure.                    |
| `licenseFileUrl` | `String`          | URL to uploaded license document.      |

#### `PreceptorResponseDTO`

| Field                     | Type        | Description                           |
|:--------------------------|:------------|:--------------------------------------|
| `userId`                  | `Long`      | Unique identifier.                    |
| `displayName`             | `String`    | Display name.                         |
| `credentials`             | `String`    | Credentials.                          |
| `specialty`               | `String`    | Specialty.                            |
| `location`                | `String`    | Location.                             |
| `setting`                 | `String`    | Clinical setting.                     |
| `availableDays`           | `List<Day>` | Days available.                       |
| `honorarium`              | `String`    | Honorarium.                           |
| `requirements`            | `String`    | Requirements text.                    |
| `isVerified`              | `boolean`   | Verification status.                  |
| `isPremium`               | `boolean`   | Premium status.                       |
| `licenseNumber`           | `String`    | License number.                       |
| `licenseState`            | `String`    | License state.                        |
| `licenseFileUrl`          | `String`    | License file link.                    |
| `verificationStatus`      | `String`    | Status (PENDING, APPROVED, REJECTED). |
| `verificationSubmittedAt` | `DateTime`  | When verification was submitted.      |
| `verificationReviewedAt`  | `DateTime`  | When verification was reviewed.       |

#### `PreceptorContactResponseDTO`

| Field   | Type     | Description                      |
|:--------|:---------|:---------------------------------|
| `phone` | `String` | Preceptor contact phone number.  |
| `email` | `String` | Preceptor contact email address. |

---

## 4. Student Management (`/students`)

| Endpoint                                         | Method | Description                            | Input                                | Output                                 |
|:-------------------------------------------------|:-------|:---------------------------------------|:-------------------------------------|:---------------------------------------|
| `/`                                              | GET    | Search and filter students.            | Query params                         | Paginated list of `StudentResponseDTO` |
| `/active/all`                                    | GET    | Fetches all active students.           | None                                 | List of `StudentResponseDTO`           |
| `/active/student-{userId}`                       | GET    | Fetches student by ID.                 | `userId` (Path)                      | `StudentResponseDTO`                   |
| `/student-{userId}`                              | PUT    | Updates student details.               | `userId` (Path), `StudentRequestDTO` | `StudentResponseDTO`                   |
| `/soft-delete/student-{userId}`                  | DELETE | Deactivates a student.                 | `userId` (Path)                      | Success message                        |
| `/hard-delete/student-{userId}`                  | DELETE | Permanently deletes a student (Admin). | `userId` (Path)                      | Success message                        |
| `/restore/student-{userId}`                      | PUT    | Restores a soft-deleted student.       | `userId` (Path)                      | Success message                        |
| `/student-{userId}/save-preceptor/{preceptorId}` | POST   | Bookmarks a preceptor.                 | `userId`, `preceptorId` (Path)       | Success message                        |
| `/student-{userId}/saved`                        | GET    | Fetches bookmarked preceptors.         | `userId` (Path)                      | List of `PreceptorResponseDTO`         |

### Data Objects

#### `StudentRequestDTO`

| Field            | Type     | Description         |
|:-----------------|:---------|:--------------------|
| `university`     | `String` | University name.    |
| `program`        | `String` | Academic program.   |
| `graduationYear` | `String` | Year of graduation. |
| `phone`          | `String` | Contact phone.      |

#### `StudentResponseDTO`

| Field            | Type     | Description        |
|:-----------------|:---------|:-------------------|
| `userId`         | `Long`   | Unique identifier. |
| `displayName`    | `String` | Display name.      |
| `email`          | `String` | Email address.     |
| `university`     | `String` | University.        |
| `program`        | `String` | Program.           |
| `graduationYear` | `String` | Graduation year.   |
| `phone`          | `String` | Phone number.      |

---

## 5. Administration (`/administration`)

### 5.1 Admin General Operations

| Endpoint                        | Method | Description                         | Input                              | Output                                 |
|:--------------------------------|:-------|:------------------------------------|:-----------------------------------|:---------------------------------------|
| `/add-admin`                    | POST   | Registers a new admin user.         | `AdminRegisterRequest`             | `AdminRegisterResponse`                |
| `/preceptors/pending`           | GET    | Fetches pending preceptor requests. | Pageable params                    | Paginated list of `Preceptor` (Entity) |
| `/preceptors/approve-{userId}`  | POST   | Approves a preceptor request.       | `userId` (Path)                    | Success message                        |
| `/preceptors/reject-{userId}`   | POST   | Rejects a preceptor request.        | `userId` (Path)                    | Success message                        |
| `/all-admins`                   | GET    | Fetches all admin users.            | None                               | List of `User` (Entity)                |
| `/user-{userId}/toggle-account` | PUT    | Enables/disables a user account.    | `userId` (Path), `enabled` (Query) | Success message                        |
| `/users`                        | GET    | List all users (admin view).        | None                               | List of `User` entities                |
| `/users/search`                 | GET    | Search users by email or name.      | `email`, `displayName` (Query)     | List of `User` entities                |
| `/user-{userId}`                | GET    | Get user by ID (admin view).        | `userId` (Path)                    | `User` entity                          |

### 5.2 Admin Dashboard & Settings

| Endpoint                | Method | Description                   | Input                        | Output                                    |
|:------------------------|:-------|:------------------------------|:-----------------------------|:------------------------------------------|
| `/dashboard`            | GET    | Get admin dashboard overview. | None                         | `AdminAnalyticsOverviewDTO`               |
| `/dashboard/report`     | GET    | Download admin dashboard report as PDF. | None               | PDF file (`Resource`)                     |
| `/settings`             | GET    | Get all system settings.      | None                         | List of `SystemSettingsDTO`               |
| `/settings/{key}`       | GET    | Get specific setting by key.  | `key` (Path)                 | `SystemSettingsDTO`                       |
| `/settings/{key}`       | PUT    | Update a system setting.      | `key` (Path), `value` (Body) | `SystemSettingsDTO`                       |
| `/revenue-summary`      | GET    | Get revenue report.           | None                         | `RevenueReportDTO`                        |
| `/revenue-transactions` | GET    | Get transaction history.      | Pageable params              | Paginated list of `TransactionHistoryDTO` |

### 5.3 Admin Revenue Management

| Endpoint                | Method | Description               | Input           | Output                                        |
|:------------------------|:-------|:--------------------------|:----------------|:----------------------------------------------|
| `/revenue/by-preceptor` | GET    | Get revenue by preceptor. | Pageable params | Paginated list of `PreceptorBillingReportDTO` |

### 5.4 Admin Analytics

| Endpoint                    | Method | Description                      | Input | Output                      |
|:----------------------------|:-------|:---------------------------------|:------|:----------------------------|
| `/analytics/overview`       | GET    | Get admin analytics overview.    | None  | `AdminAnalyticsOverviewDTO` |
| `/analytics/top-preceptors` | GET    | Get top preceptors by analytics. | None  | `AdminAnalyticsOverviewDTO` |
| `/analytics/trends`         | GET    | Get platform trends.             | None  | `AdminAnalyticsOverviewDTO` |

### 5.5 Admin Preceptor Management

| Endpoint                                    | Method | Description                                       | Input                                                   | Output                                    |
|:--------------------------------------------|:-------|:--------------------------------------------------|:--------------------------------------------------------|:------------------------------------------|
| `/preceptors/list`                          | GET    | List all preceptors (admin view).                 | Pageable params                                         | Paginated list of `AdminPreceptorListDTO` |
| `/preceptors/list/search`                   | GET    | Search and filter preceptors (admin view).        | `specialty`, `location`, `verificationStatus`, Pageable | Paginated list of `AdminPreceptorListDTO` |
| `/preceptors/verified/approved`             | GET    | Get approved preceptors.                          | Pageable params                                         | Paginated list of `AdminPreceptorListDTO` |
| `/preceptors/verified/rejected`             | GET    | Get rejected preceptors.                          | Pageable params                                         | Paginated list of `AdminPreceptorListDTO` |
| `/preceptors/detail-{userId}`               | GET    | Get preceptor detail (admin view).                | `userId` (Path)                                         | `AdminPreceptorDetailDTO`                 |
| `/preceptors/update-{userId}`               | PUT    | Update preceptor (admin).                         | `userId` (Path), `AdminPreceptorDetailDTO`              | `AdminPreceptorDetailDTO`                 |
| `/preceptors/{userId}/verification-history` | GET    | Get verification history for preceptor.           | `userId` (Path)                                         | List of `VerificationHistoryDTO`          |
| `/preceptors/{userId}/verification-notes`   | POST   | Add verification note to preceptor.               | `userId` (Path), `note`, `noteType` (Query)             | Success message                           |
| `/preceptors/detail-{userId}/reject`        | POST   | Reject preceptor with rejection reason.           | `userId` (Path), `reason` (Query)                       | Success message                           |
| `/preceptors/{userId}/billing`              | GET    | Get preceptor billing report.                     | `userId` (Path)                                         | `PreceptorBillingReportDTO`               |
| `/preceptors/{userId}/analytics`            | GET    | Get preceptor analytics.                          | `userId` (Path)                                         | `PreceptorAnalyticsDTO`                   |
| `/preceptors/detail-{userId}/contact`       | GET    | Get preceptor contact (admin - no premium check). | `userId` (Path)                                         | `PreceptorContactResponseDTO`             |
| `/preceptors/{userId}/license/download`     | GET    | Download preceptor license file (admin).          | `userId` (Path)                                         | License file (PDF Resource)               |
| `/preceptors/{userId}/license/review`       | GET    | View preceptor license image (admin).             | `userId` (Path)                                         | License image (Resource)                  |

### 5.6 Admin Student Management

| Endpoint                              | Method | Description                              | Input                                    | Output                                  |
|:--------------------------------------|:-------|:-----------------------------------------|:-----------------------------------------|:----------------------------------------|
| `/students/list`                      | GET    | List all students (admin view).          | Pageable params                          | Paginated list of `AdminStudentListDTO` |
| `/students/search`                    | GET    | Search and filter students (admin view). | `university`, `program`, Pageable        | Paginated list of `AdminStudentListDTO` |
| `/students/detail-{userId}`           | GET    | Get student detail (admin view).         | `userId` (Path)                          | `AdminStudentDetailDTO`                 |
| `/students/update-{userId}`           | PUT    | Update student (admin).                  | `userId` (Path), `AdminStudentDetailDTO` | `AdminStudentDetailDTO`                 |
| `/students/update-{userId}`           | DELETE | Delete student (soft delete).            | `userId` (Path)                          | Success message                         |
| `/students/detail-{userId}/inquiries` | GET    | Get student inquiries.                   | `userId` (Path)                          | List of inquiries                       |

### 5.7 Admin Webhook Management

| Endpoint                          | Method | Description                         | Input            | Output                                   |
|:----------------------------------|:-------|:------------------------------------|:-----------------|:-----------------------------------------|
| `/webhooks/history`               | GET    | Get webhook event history.          | Pageable params  | Paginated list of `WebhookEventResponse` |
| `/webhooks/event-{eventId}/retry` | POST   | Retry failed webhook event.         | `eventId` (Path) | Success message                          |
| `/webhooks/event-{eventId}`       | GET    | Get webhook event details.          | `eventId` (Path) | `WebhookEventDetailDTO`                  |
| `/webhooks/metrics`               | GET    | Get webhook metrics and statistics. | None             | `WebhookMetricsDTO`                      |

### 5.8 Admin Credentials & Specialties Management

| Endpoint                                              | Method | Description                                | Input                                            | Output                  |
|:------------------------------------------------------|:-------|:-------------------------------------------|:-------------------------------------------------|:------------------------|
| `/credentials-specialties/credentials`                | GET    | Get all credentials (predefined and user). | None                                             | List of `CredentialDTO` |
| `/credentials-specialties/credentials`                | POST   | Create a new credential.                   | `CreateCredentialRequest`                        | `CredentialDTO`         |
| `/credentials-specialties/credentials/{credentialId}` | PUT    | Update a credential.                       | `credentialId` (Path), `CreateCredentialRequest` | `CredentialDTO`         |
| `/credentials-specialties/credentials/{credentialId}` | DELETE | Delete a credential.                       | `credentialId` (Path)                            | Success message         |
| `/credentials-specialties/specialties`                | GET    | Get all specialties (predefined and user). | None                                             | List of `SpecialtyDTO`  |
| `/credentials-specialties/specialties`                | POST   | Create a new specialty.                    | `CreateSpecialtyRequest`                         | `SpecialtyDTO`          |
| `/credentials-specialties/specialties/{specialtyId}`  | PUT    | Update a specialty.                        | `specialtyId` (Path), `CreateSpecialtyRequest`   | `SpecialtyDTO`          |
| `/credentials-specialties/specialties/{specialtyId}`  | DELETE | Delete a specialty.                        | `specialtyId` (Path)                             | Success message         |

### Data Objects for Administration

#### `AdminAnalyticsOverviewDTO`

| Field                  | Type         | Description                |
|:-----------------------|:-------------|:---------------------------|
| `totalUsers`           | `Long`       | Total user count.          |
| `totalStudents`        | `Long`       | Total student count.       |
| `totalPreceptors`      | `Long`       | Total preceptor count.     |
| `newUsersThisMonth`    | `Long`       | New users this month.      |
| `verifiedPreceptors`   | `Long`       | Verified preceptor count.  |
| `pendingVerifications` | `Long`       | Pending verifications.     |
| `totalRevenue`         | `BigDecimal` | Total revenue.             |
| `revenueThisMonth`     | `BigDecimal` | Revenue this month.        |
| `totalSubscriptions`   | `Long`       | Total subscription count.  |
| `activeSubscriptions`  | `Long`       | Active subscription count. |

#### `AdminPreceptorListDTO`

| Field                | Type            | Description                  |
|:---------------------|:----------------|:-----------------------------|
| `userId`             | `Long`          | Unique identifier.           |
| `displayName`        | `String`        | Display name.                |
| `email`              | `String`        | Email address.               |
| `specialty`          | `String`        | Specialty area.              |
| `location`           | `String`        | Location.                    |
| `verificationStatus` | `String`        | Verification status.         |
| `isPremium`          | `boolean`       | Premium subscription status. |
| `createdAt`          | `LocalDateTime` | Creation timestamp.          |

#### `AdminPreceptorDetailDTO`

| Field                | Type      | Description                 |
|:---------------------|:----------|:----------------------------|
| `userId`             | `Long`    | Unique identifier.          |
| `displayName`        | `String`  | Display name.               |
| `email`              | `String`  | Email address.              |
| `credentials`        | `String`  | Professional credentials.   |
| `specialty`          | `String`  | Specialty area.             |
| `location`           | `String`  | Location.                   |
| `phone`              | `String`  | Phone number.               |
| `honorarium`         | `String`  | Honorarium.                 |
| `requirements`       | `String`  | Requirements.               |
| `isVerified`         | `boolean` | Verification status.        |
| `isPremium`          | `boolean` | Premium status.             |
| `verificationStatus` | `String`  | Verification status detail. |

#### `AdminStudentListDTO`

| Field         | Type            | Description         |
|:--------------|:----------------|:--------------------|
| `userId`      | `Long`          | Unique identifier.  |
| `displayName` | `String`        | Display name.       |
| `email`       | `String`        | Email address.      |
| `university`  | `String`        | University name.    |
| `program`     | `String`        | Academic program.   |
| `createdAt`   | `LocalDateTime` | Creation timestamp. |

#### `AdminStudentDetailDTO`

| Field             | Type      | Description                |
|:------------------|:----------|:---------------------------|
| `userId`          | `Long`    | Unique identifier.         |
| `displayName`     | `String`  | Display name.              |
| `email`           | `String`  | Email address.             |
| `phone`           | `String`  | Phone number.              |
| `university`      | `String`  | University name.           |
| `program`         | `String`  | Academic program.          |
| `graduationYear`  | `String`  | Graduation year.           |
| `savedPreceptors` | `Integer` | Count of saved preceptors. |
| `inquiriesSent`   | `Integer` | Count of inquiries sent.   |

#### `VerificationHistoryDTO`

| Field             | Type            | Description         |
|:------------------|:----------------|:--------------------|
| `auditId`         | `Long`          | Audit log ID.       |
| `previousStatus`  | `String`        | Previous status.    |
| `newStatus`       | `String`        | New status.         |
| `reviewerUserId`  | `Long`          | Reviewer user ID.   |
| `reviewNote`      | `String`        | Review note/reason. |
| `changeTimestamp` | `LocalDateTime` | Change timestamp.   |

#### `PreceptorBillingReportDTO`

| Field                 | Type            | Description                     |
|:----------------------|:----------------|:--------------------------------|
| `userId`              | `Long`          | Unique identifier.              |
| `displayName`         | `String`        | Display name.                   |
| `accountStatus`       | `String`        | Account status.                 |
| `subscriptionTier`    | `String`        | Subscription tier.              |
| `successfulRevenue`   | `BigDecimal`    | Successful transaction revenue. |
| `failedRevenue`       | `BigDecimal`    | Failed transaction revenue.     |
| `successCount`        | `Integer`       | Successful transaction count.   |
| `lastInvoiceDate`     | `LocalDateTime` | Last invoice date.              |
| `lastTransactionDate` | `LocalDateTime` | Last transaction date.          |

#### `PreceptorAnalyticsDTO`

| Field                    | Type      | Description                   |
|:-------------------------|:----------|:------------------------------|
| `userId`                 | `Long`    | Unique identifier.            |
| `displayName`            | `String`  | Display name.                 |
| `profileViews`           | `Long`    | Total profile views.          |
| `contactReveals`         | `Long`    | Total contact reveals.        |
| `inquiries`              | `Long`    | Total inquiries received.     |
| `responseRate`           | `Double`  | Response rate percentage.     |
| `successfulTransactions` | `Integer` | Successful transaction count. |
| `conversionRate`         | `Double`  | Conversion rate percentage.   |
| `totalTransactions`      | `Long`    | Total transaction count.      |

#### `WebhookEventDetailDTO`

| Field          | Type            | Description             |
|:---------------|:----------------|:------------------------|
| `eventId`      | `String`        | Webhook event ID.       |
| `eventType`    | `String`        | Event type.             |
| `status`       | `String`        | Processing status.      |
| `processedAt`  | `LocalDateTime` | Processing timestamp.   |
| `retryCount`   | `Integer`       | Number of retries.      |
| `errorMessage` | `String`        | Error message (if any). |

#### `WebhookMetricsDTO`

| Field                   | Type      | Description                   |
|:------------------------|:----------|:------------------------------|
| `totalEvents`           | `Long`    | Total webhook events.         |
| `successfulEvents`      | `Long`    | Successful events.            |
| `failedEvents`          | `Long`    | Failed events.                |
| `pendingRetries`        | `Integer` | Pending retry count.          |
| `averageProcessingTime` | `Long`    | Average processing time (ms). |

#### `SystemSettingsDTO`

| Field         | Type      | Description                |
|:--------------|:----------|:---------------------------|
| `settingKey`  | `String`  | Setting key identifier.    |
| `value`       | `Object`  | Setting value.             |
| `description` | `String`  | Setting description.       |
| `isActive`    | `boolean` | Whether setting is active. |

#### `RevenueReportDTO`

| Field                    | Type         | Description                   |
|:-------------------------|:-------------|:------------------------------|
| `totalRevenue`           | `BigDecimal` | Total revenue amount.         |
| `revenueThisMonth`       | `BigDecimal` | Revenue this month.           |
| `successfulTransactions` | `Long`       | Successful transaction count. |
| `failedTransactions`     | `Long`       | Failed transaction count.     |
| `totalTransactions`      | `Long`       | Total transaction count.      |

#### `TransactionHistoryDTO`

| Field                | Type            | Description            |
|:---------------------|:----------------|:-----------------------|
| `transactionId`      | `Long`          | Transaction ID.        |
| `preceptorId`        | `Long`          | Preceptor ID.          |
| `preceptorName`      | `String`        | Preceptor name.        |
| `amountInMinorUnits` | `Long`          | Amount in minor units. |
| `currency`           | `String`        | Currency code.         |
| `status`             | `String`        | Transaction status.    |
| `transactionAt`      | `LocalDateTime` | Transaction timestamp. |

#### `AdminRegisterRequest`

| Field         | Type     | Description          |
|:--------------|:---------|:---------------------|
| `email`       | `String` | Admin email address. |
| `password`    | `String` | Admin password.      |
| `displayName` | `String` | Admin display name.  |

#### `AdminRegisterResponse`

| Field         | Type     | Description        |
|:--------------|:---------|:-------------------|
| `userId`      | `Long`   | Unique identifier. |
| `displayName` | `String` | Display name.      |
| `email`       | `String` | Email address.     |
| `role`        | `String` | Assigned role.     |

#### `CredentialDTO`

| Field          | Type      | Description                              |
|:---------------|:----------|:-----------------------------------------|
| `id`           | `Long`    | Credential ID.                           |
| `name`         | `String`  | Name of the credential (e.g., MBBS, MD). |
| `description`  | `String`  | Description of the credential.           |
| `isPredefined` | `boolean` | Whether credential is system-predefined. |

#### `CreateCredentialRequest`

| Field         | Type     | Description                    |
|:--------------|:---------|:-------------------------------|
| `name`        | `String` | Name of the credential.        |
| `description` | `String` | Description of the credential. |

#### `SpecialtyDTO`

| Field          | Type      | Description                               |
|:---------------|:----------|:------------------------------------------|
| `id`           | `Long`    | Specialty ID.                             |
| `name`         | `String`  | Name of the specialty (e.g., Cardiology). |
| `description`  | `String`  | Description of the specialty.             |
| `isPredefined` | `boolean` | Whether specialty is system-predefined.   |

#### `CreateSpecialtyRequest`

| Field         | Type     | Description                   |
|:--------------|:---------|:------------------------------|
| `name`        | `String` | Name of the specialty.        |
| `description` | `String` | Description of the specialty. |

---

## 6. Analytics (`/analytics`)

| Endpoint                 | Method | Description                         | Input                   | Output                   |
|:-------------------------|:-------|:------------------------------------|:------------------------|:-------------------------|
| `/event`                 | POST   | Logs an analytics event.            | `AnalyticsEventRequest` | None                     |
| `/preceptors/{id}/stats` | GET    | Fetches statistics for a preceptor. | `id` (Path)             | `PreceptorStatsResponse` |

### Data Objects

#### `AnalyticsEventRequest`

| Field         | Type     | Description                                         |
|:--------------|:---------|:----------------------------------------------------|
| `eventType`   | `String` | Type of event (e.g., PROFILE_VIEW, CONTACT_REVEAL). |
| `preceptorId` | `Long`   | Target preceptor ID.                                |

#### `PreceptorStatsResponse`

| Field            | Type   | Description            |
|:-----------------|:-------|:-----------------------|
| `profileViews`   | `Long` | Total profile views.   |
| `contactReveals` | `Long` | Total contact reveals. |
| `inquiries`      | `Long` | Total inquiries.       |

---

## 7. Role Management (`/roles`)

| Endpoint                | Method | Description                     | Input                            | Output                    |
|:------------------------|:-------|:--------------------------------|:---------------------------------|:--------------------------|
| `/active/all`           | GET    | Fetch all active roles.         | None                             | List of `RoleResponseDTO` |
| `/active/role-{roleId}` | GET    | Fetch a role by ID.             | `roleId` (Path)                  | `RoleResponseDTO`         |
| `/`                     | POST   | Create a new role (admin only). | `RoleCreateDTO`                  | `RoleResponseDTO`         |
| `/role-{roleId}`        | PUT    | Update a role (admin only).     | `roleId` (Path), `RoleUpdateDTO` | `RoleResponseDTO`         |
| `/role-{roleId}`        | DELETE | Delete a role (admin only).     | `roleId` (Path)                  | Success message           |

### Data Objects

#### `RoleResponseDTO`

| Field         | Type     | Description                                   |
|:--------------|:---------|:----------------------------------------------|
| `roleId`      | `Long`   | Unique role identifier.                       |
| `roleName`    | `String` | Name of the role (STUDENT, PRECEPTOR, ADMIN). |
| `description` | `String` | Brief description of the role.                |

#### `RoleCreateDTO`

| Field         | Type     | Description              |
|:--------------|:---------|:-------------------------|
| `roleName`    | `String` | Name of the new role.    |
| `description` | `String` | Description of the role. |

#### `RoleUpdateDTO`

| Field         | Type     | Description               |
|:--------------|:---------|:--------------------------|
| `description` | `String` | Updated role description. |

---

## 8. Subscriptions (`/api/subscriptions`)

| Endpoint          | Method | Description                                                         | Input                          | Output                              |
|:------------------|:-------|:--------------------------------------------------------------------|:-------------------------------|:------------------------------------|
| `/checkout`       | POST   | Initiates subscription via Stripe checkout.                         | `CreateCheckoutSessionRequest` | `CreateCheckoutSessionResponse`     |
| `/status`         | GET    | Fetch current subscription details.                                 | None                           | `SubscriptionDetailResponse`        |
| `/cancel`         | POST   | Cancel subscription at period end (allows usage until expiry).      | None                           | `GenericApiResponse<Void>`          |
| `/update`         | PUT    | Change subscription plan or billing interval.                       | `UpdateSubscriptionRequest`    | `GenericApiResponse<Void>`          |
| `/history`        | GET    | List past and current subscriptions with pagination.                | Pageable params                | `Page<SubscriptionHistoryResponse>` |
| `/billing-portal` | GET    | Redirect to Stripe customer portal for billing management.          | None                           | Map with `portalUrl` key            |
| `/access-check`   | GET    | Verify if user can access premium features (includes grace period). | None                           | Map with `hasAccess` boolean key    |
| `/events`         | GET    | Retrieve audit trail of all subscription lifecycle events.          | Pageable params                | `Page<SubscriptionEventResponse>`   |

### Data Objects

#### `CreateCheckoutSessionRequest`

| Field     | Type   | Description      |
|:----------|:-------|:-----------------|
| `priceId` | `Long` | Stripe price ID. |

#### `CreateCheckoutSessionResponse`

| Field         | Type     | Description            |
|:--------------|:---------|:-----------------------|
| `sessionId`   | `String` | Stripe session ID.     |
| `checkoutUrl` | `String` | Checkout URL for user. |
| `customerId`  | `String` | Stripe customer ID.    |

#### `SubscriptionDetailResponse`

| Field                | Type            | Description                        |
|:---------------------|:----------------|:-----------------------------------|
| `subscriptionId`     | `Long`          | Unique subscription identifier.    |
| `planCode`           | `String`        | Plan code.                         |
| `planName`           | `String`        | Plan name.                         |
| `billingInterval`    | `String`        | Billing interval (monthly/yearly). |
| `amountInMinorUnits` | `Long`          | Amount in minor currency units.    |
| `currency`           | `String`        | Currency code.                     |
| `status`             | `String`        | Subscription status.               |
| `accessEnabled`      | `boolean`       | Whether access is enabled.         |
| `currentPeriodStart` | `LocalDateTime` | Current period start date.         |
| `currentPeriodEnd`   | `LocalDateTime` | Current period end date.           |
| `trialEndsAt`        | `LocalDateTime` | Trial end date (if applicable).    |
| `cancelAtPeriodEnd`  | `boolean`       | Whether cancellation is pending.   |
| `canceledAt`         | `LocalDateTime` | Cancellation date (if applicable). |
| `canceledReason`     | `String`        | Reason for cancellation.           |

#### `UpdateSubscriptionRequest`

| Field     | Type   | Description          |
|:----------|:-------|:---------------------|
| `priceId` | `Long` | New Stripe price ID. |

#### `SubscriptionHistoryResponse`

| Field            | Type            | Description              |
|:-----------------|:----------------|:-------------------------|
| `subscriptionId` | `Long`          | Subscription ID.         |
| `planCode`       | `String`        | Plan code.               |
| `planName`       | `String`        | Plan name.               |
| `status`         | `String`        | Subscription status.     |
| `startDate`      | `LocalDateTime` | Subscription start date. |
| `endDate`        | `LocalDateTime` | Subscription end date.   |
| `cancelReason`   | `String`        | Cancellation reason.     |

#### `SubscriptionEventResponse`

| Field            | Type                  | Description                                              |
|:-----------------|:----------------------|:---------------------------------------------------------|
| `eventId`        | `Long`                | Unique subscription event identifier.                    |
| `subscriptionId` | `Long`                | Subscription ID this event belongs to.                   |
| `eventType`      | `String`              | Type of event (CREATED, CANCELLED, PLAN_UPGRADED, etc.). |
| `status`         | `String`              | Event processing status (SUCCESS, FAILED).               |
| `createdAt`      | `LocalDateTime`       | When the event occurred.                                 |
| `details`        | `Map<String, Object>` | JSON metadata with event-specific details.               |
| `stripeEventId`  | `String`              | Stripe webhook event ID (for idempotency).               |
| `errorMessage`   | `String`              | Error message if event processing failed.                |

---

## 9. Subscription Plans (`/subscription-plans`)

| Endpoint | Method | Description                        | Input | Output                           |
|:---------|:-------|:-----------------------------------|:------|:---------------------------------|
| `/`      | GET    | Get all active subscription plans. | None  | `List<SubscriptionPlanResponse>` |

### Data Objects

#### `SubscriptionPlanResponse`

| Field                | Type                              | Description             |
|:---------------------|:----------------------------------|:------------------------|
| `subscriptionPlanId` | `Long`                            | Plan ID.                |
| `code`               | `String`                          | Plan code.              |
| `name`               | `String`                          | Plan name.              |
| `description`        | `String`                          | Plan description.       |
| `active`             | `boolean`                         | Whether plan is active. |
| `prices`             | `List<SubscriptionPriceResponse>` | List of prices.         |

#### `SubscriptionPriceResponse`

| Field                 | Type      | Description                        |
|:----------------------|:----------|:-----------------------------------|
| `subscriptionPriceId` | `Long`    | Price ID.                          |
| `billingInterval`     | `String`  | Billing interval (monthly/yearly). |
| `currency`            | `String`  | Currency code.                     |
| `amountInMinorUnits`  | `Long`    | Amount in minor currency units.    |
| `active`              | `boolean` | Whether price is active.           |

---

## 10. Payments (`/api/payments`)

| Endpoint                   | Method | Description                                       | Input                    | Output                    |
|:---------------------------|:-------|:--------------------------------------------------|:-------------------------|:--------------------------|
| `/create-checkout-session` | POST   | Create a Stripe checkout session for a preceptor. | `CheckoutSessionRequest` | `CheckoutSessionResponse` |

### Data Objects

#### `CheckoutSessionRequest`

| Field          | Type           | Description                |
|:---------------|:---------------|:---------------------------|
| `preceptorId`  | `Long`         | Preceptor ID.              |
| `billingCycle` | `BillingCycle` | Billing cycle.             |
| `successUrl`   | `String`       | URL on successful payment. |
| `cancelUrl`    | `String`       | URL on canceled payment.   |

#### `CheckoutSessionResponse`

| Field         | Type     | Description                 |
|:--------------|:---------|:----------------------------|
| `sessionId`   | `String` | Stripe session ID.          |
| `checkoutUrl` | `String` | Checkout URL for preceptor. |

---

## 11. Webhooks (`/webhooks`)

| Endpoint  | Method | Description                            | Input                               | Output                       |
|:----------|:-------|:---------------------------------------|:------------------------------------|:-----------------------------|
| `/`       | POST   | Receive Stripe webhook events.         | Raw Stripe event payload (`String`) | `GenericApiResponse<Void>`   |
| `/events` | GET    | Get webhook event history (paginated). | Pageable params                     | `Page<WebhookEventResponse>` |

### Data Objects

#### `WebhookEventResponse`

| Field          | Type            | Description               |
|:---------------|:----------------|:--------------------------|
| `eventId`      | `String`        | Webhook event ID.         |
| `eventType`    | `String`        | Event type.               |
| `status`       | `String`        | Processing status.        |
| `processedAt`  | `LocalDateTime` | Processing timestamp.     |
| `retryCount`   | `Integer`       | Number of retries.        |
| `errorMessage` | `String`        | Error message (if error). |

---

## 12. Inquiries (`/inquiries`)

| Endpoint            | Method | Description                                                       | Input                            | Output                     |
|:--------------------|:-------|:------------------------------------------------------------------|:---------------------------------|:---------------------------|
| `/send`             | POST   | Student sends an inquiry to a preceptor.                          | `InquiryRequestDTO`              | `InquiryResponseDTO`       |
| `/my-inquiries`     | GET    | Get all inquiries for the authenticated user, filtered by status. | `inquiryStatus`, Pageable params | `Page<InquiryResponseDTO>` |
| `/{inquiryId}/read` | PATCH  | Mark an inquiry as read.                                          | `inquiryId` (Path)               | `GenericApiResponse<Void>` |

### Data Objects

#### `InquiryRequestDTO`

| Field         | Type     | Description          |
|:--------------|:---------|:---------------------|
| `preceptorId` | `Long`   | ID of the preceptor. |
| `subject`     | `String` | Inquiry subject.     |
| `message`     | `String` | Inquiry message.     |

#### `InquiryResponseDTO`

| Field         | Type            | Description         |
|:--------------|:----------------|:--------------------|
| `inquiryId`   | `Long`          | Inquiry ID.         |
| `studentName` | `String`        | Student name.       |
| `subject`     | `String`        | Inquiry subject.    |
| `message`     | `String`        | Inquiry message.    |
| `status`      | `InquiryStatus` | Inquiry status.     |
| `createdAt`   | `LocalDateTime` | Creation timestamp. |

---

## 13. User Profile Management (`/users`)

| Endpoint                                | Method | Description                        | Input              | Output                |
|:----------------------------------------|:-------|:-----------------------------------|:-------------------|:----------------------|
| `/user-{userId}/upload-profile-picture` | PUT    | Upload a user's profile picture.   | `file` (Multipart) | `UserResponseDTO`     |
| `/user-{userId}/profile-picture`        | GET    | Download a user's profile picture. | `userId` (Path)    | Image file (Resource) |

---

## 14. Preceptor License Management (`/preceptors`)

| Endpoint                           | Method | Description                                         | Input           | Output                      |
|:-----------------------------------|:-------|:----------------------------------------------------|:----------------|:----------------------------|
| `/preceptor-{userId}/license`      | GET    | Download preceptor license file.                    | `userId` (Path) | License file (PDF Resource) |
| `/preceptor-{userId}/license/view` | GET    | View preceptor license image inline in the browser. | `userId` (Path) | License image/PDF resource  |

---

## 15. System Health (`/`)

| Endpoint | Method | Description          | Input | Output                 |
|:---------|:-------|:---------------------|:------|:-----------------------|
| `/`      | GET    | System health check. | None  | `SystemHealthResponse` |

### Data Objects

#### `SystemHealthResponse`

| Field     | Type     | Description                                        |
|:----------|:---------|:---------------------------------------------------|
| `service` | `String` | Service name (e.g., "NPaxis Backend").             |
| `status`  | `String` | Service status (e.g., "UP").                       |
| `auth`    | `String` | Auth configuration status.                         |
| `health`  | `String` | URL to health endpoint (e.g., "/actuator/health"). |

---

## Notes

- **Base URL**: All endpoints are prefixed with `/api/v1`.
- **Authentication**: Most endpoints require JWT authentication via the `Authorization` header with a Bearer token.
- **Pagination**: Endpoints with pageable responses support pagination parameters:
    - `page`: Page number (0-indexed)
    - `size`: Number of items per page
    - `sort`: Sort by field (e.g., `sort=createdAt,desc`)
- **Multipart Uploads**: Some endpoints support file uploads via `multipart/form-data`.
- **Response Format**: All responses follow the `GenericApiResponse<T>` wrapper format with:
    - `data`: Actual response data
    - `message`: Human-readable message
    - `success`: Boolean indicating success/failure
    - `timestamp`: Response timestamp
