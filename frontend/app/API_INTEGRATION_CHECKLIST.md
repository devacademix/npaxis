# Frontend API Integration Checklist

This checklist tracks frontend coverage for the documented backend APIs.

## Authentication

- [x] `POST /api/v1/auth/login`
- [x] `POST /api/v1/auth/register`
- [x] `POST /api/v1/auth/refresh-token`
- [x] `POST /api/v1/auth/logout`
- [x] `POST /api/v1/auth/verify-otp`
- [x] `POST /api/v1/auth/forgot-password`
- [x] `POST /api/v1/auth/reset-password`
- [x] `POST /api/v1/auth/initialize`

## Inquiries

- [x] `POST /api/v1/inquiries/send`
- [x] `GET /api/v1/inquiries/my-inquiries`
- [x] `PATCH /api/v1/inquiries/{inquiryId}/read`
- [x] Student inquiries page route
- [x] Preceptor inquiries inbox route

## Subscriptions and Plans

- [x] `GET /api/v1/subscription-plans`
- [x] `POST /api/v1/subscriptions/checkout`
- [x] `GET /api/v1/subscriptions/status`
- [x] `POST /api/v1/subscriptions/cancel`
- [ ] `PUT /api/v1/subscriptions/update`
- [x] `GET /api/v1/subscriptions/history`
- [x] `GET /api/v1/subscriptions/billing-portal`
- [x] `GET /api/v1/subscriptions/access-check`
- [x] Subscription UI wired to documented endpoints
- [x] Billing UI wired to documented endpoints

## Admin Dashboard, Settings, Revenue, Analytics

- [x] `GET /api/v1/administration/dashboard`
- [x] `GET /api/v1/administration/settings`
- [x] `PUT /api/v1/administration/settings/{key}`
- [ ] `GET /api/v1/administration/settings/{key}`
- [x] `GET /api/v1/administration/revenue-summary`
- [x] `GET /api/v1/administration/revenue-transactions`
- [ ] `GET /api/v1/administration/revenue/by-preceptor`
- [x] `GET /api/v1/administration/analytics/overview`
- [ ] `GET /api/v1/administration/analytics/top-preceptors`
- [ ] `GET /api/v1/administration/analytics/trends`

## Admin Preceptor Management

- [x] `GET /api/v1/administration/preceptors/list`
- [x] `GET /api/v1/administration/preceptors/list/search`
- [ ] `GET /api/v1/administration/preceptors/verified/approved`
- [ ] `GET /api/v1/administration/preceptors/verified/rejected`
- [x] `GET /api/v1/administration/preceptors/detail-{userId}`
- [x] `PUT /api/v1/administration/preceptors/update-{userId}`
- [x] `GET /api/v1/administration/preceptors/{userId}/verification-history`
- [x] `POST /api/v1/administration/preceptors/{userId}/verification-notes`
- [x] `POST /api/v1/administration/preceptors/detail-{userId}/reject`
- [ ] `GET /api/v1/administration/preceptors/{userId}/billing`
- [ ] `GET /api/v1/administration/preceptors/{userId}/analytics`
- [ ] `GET /api/v1/administration/preceptors/detail-{userId}/contact`
- [x] `GET /api/v1/administration/preceptors/{userId}/license/download`
- [x] `GET /api/v1/administration/preceptors/{userId}/license/review`

## Admin Student Management

- [ ] `GET /api/v1/administration/students/list`
- [x] `GET /api/v1/administration/students/search`
- [x] `GET /api/v1/administration/students/detail-{userId}`
- [x] `PUT /api/v1/administration/students/update-{userId}`
- [x] `DELETE /api/v1/administration/students/update-{userId}`
- [x] `GET /api/v1/administration/students/detail-{userId}/inquiries`

## Roles

- [x] `GET /api/v1/roles/active/all`
- [x] `GET /api/v1/roles/active/role-{roleId}`
- [x] `POST /api/v1/roles`
- [x] `PUT /api/v1/roles/role-{roleId}`
- [x] `DELETE /api/v1/roles/role-{roleId}`

## User Media and License Media

- [x] `PUT /api/v1/users/user-{userId}/upload-profile-picture`
- [x] `GET /api/v1/users/user-{userId}/profile-picture`
- [x] `GET /api/v1/preceptors/preceptor-{userId}/license`
- [x] `GET /api/v1/preceptors/preceptor-{userId}/license/view`
- [x] Student profile picture UI
- [x] Preceptor profile picture UI
- [x] Preceptor license preview/download UI

## Remaining Optional Admin/Infra Work

- [ ] Admin webhook management UI
- [ ] User deleted listing UI
- [ ] System health status UI
- [ ] Admin analytics dedicated page
