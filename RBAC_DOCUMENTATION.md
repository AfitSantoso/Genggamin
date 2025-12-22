# RBAC Implementation Documentation

## Overview

Implementasi Role-Based Access Control (RBAC) untuk sistem loan management dengan 5 role utama:

- **ADMIN**: Akses penuh ke semua fitur
- **CUSTOMER**: Dapat submit loan application
- **MARKETING**: Dapat review loan applications
- **BRANCH_MANAGER**: Dapat approve/reject loans
- **BACK_OFFICE**: Dapat disburse loans

## Roles dan Permissions

### 1. CUSTOMER

**Endpoints yang dapat diakses:**

- `POST /loans/submit` - Submit loan application
- `GET /loans/my-loans` - View own loan applications
- `GET /loans/{loanId}` - View loan details

### 2. MARKETING

**Endpoints yang dapat diakses:**

- `GET /loans/review` - Get list of submitted loans
- `POST /loans/review/{loanId}` - Review a loan (mark as UNDER_REVIEW)
- `GET /loans/{loanId}` - View loan details

### 3. BRANCH_MANAGER

**Endpoints yang dapat diakses:**

- `GET /loans/approve` - Get list of reviewed loans
- `POST /loans/approve/{loanId}` - Approve or reject a loan
- `GET /loans/{loanId}` - View loan details

### 4. BACK_OFFICE

**Endpoints yang dapat diakses:**

- `GET /loans/disburse` - Get list of approved loans
- `POST /loans/disburse/{loanId}` - Disburse a loan
- `GET /loans/{loanId}` - View loan details

### 5. ADMIN

**Endpoints yang dapat diakses:**

- Semua endpoint yang tersedia (full access)
- `GET /loans/all` - View all loans in the system

## Loan Workflow

```
SUBMITTED → UNDER_REVIEW → APPROVED → DISBURSED
              ↓
           REJECTED
```

1. **CUSTOMER** submits loan → Status: `SUBMITTED`
2. **MARKETING** reviews loan → Status: `UNDER_REVIEW`
3. **BRANCH_MANAGER** approves/rejects → Status: `APPROVED` or `REJECTED`
4. **BACK_OFFICE** disburses → Status: `DISBURSED`

## API Endpoints

### Register User with Role

```http
POST /auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "+628123456789",
  "roles": ["CUSTOMER"]
}
```

Jika `roles` tidak disediakan, default role adalah `CUSTOMER`.

### Login

```http
POST /auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

Response:

```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "isActive": true,
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Submit Loan (CUSTOMER)

```http
POST /loans/submit
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 50000000,
  "tenureMonths": 12,
  "purpose": "Business expansion"
}
```

### Get Loans for Review (MARKETING)

```http
GET /loans/review
Authorization: Bearer <token>
```

### Review Loan (MARKETING)

```http
POST /loans/review/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "notes": "Customer documents are complete. Proceeding to approval."
}
```

### Get Loans for Approval (BRANCH_MANAGER)

```http
GET /loans/approve
Authorization: Bearer <token>
```

### Approve/Reject Loan (BRANCH_MANAGER)

```http
POST /loans/approve/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "approved": true,
  "notes": "Loan approved based on customer credit history."
}
```

### Get Loans for Disbursement (BACK_OFFICE)

```http
GET /loans/disburse
Authorization: Bearer <token>
```

### Disburse Loan (BACK_OFFICE)

```http
POST /loans/disburse/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "notes": "Funds disbursed to customer account."
}
```

### Get All Loans (ADMIN)

```http
GET /loans/all
Authorization: Bearer <token>
```

## Default Users

Sistem akan membuat user default saat startup:

| Username    | Password | Role           | Email                   |
| ----------- | -------- | -------------- | ----------------------- |
| admin       | password | ADMIN          | admin@example.com       |
| customer1   | password | CUSTOMER       | customer1@example.com   |
| marketing1  | password | MARKETING      | marketing1@example.com  |
| manager1    | password | BRANCH_MANAGER | manager1@example.com    |
| backoffice1 | password | BACK_OFFICE    | backoffice1@example.com |

## Security Implementation

### 1. Method-Level Security

Menggunakan `@PreAuthorize` annotation untuk mengamankan endpoints:

```java
@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
public ResponseEntity<ApiResponse<LoanResponse>> submitLoan(...)
```

### 2. JWT Token with Roles

Token JWT menyimpan roles dalam claims:

```json
{
  "sub": "john_doe",
  "roles": ["CUSTOMER"],
  "iat": 1234567890,
  "exp": 1234571490
}
```

### 3. Role-Based Authorization

Filter JWT mengekstrak roles dari token dan set ke Spring Security context dengan prefix `ROLE_`.

## Error Responses

### Unauthorized (401)

Ketika token invalid atau expired:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired bearer token"
}
```

### Forbidden (403)

Ketika user tidak memiliki role yang sesuai:

```json
{
  "success": false,
  "message": "Access Denied"
}
```

### Bad Request (400)

Ketika request invalid:

```json
{
  "success": false,
  "message": "Loan is not in SUBMITTED status"
}
```

## Testing dengan Postman/cURL

### 1. Register sebagai Customer

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer_test",
    "email": "customer@test.com",
    "password": "test123",
    "roles": ["CUSTOMER"]
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer_test",
    "password": "test123"
  }'
```

### 3. Submit Loan

```bash
curl -X POST http://localhost:8080/loans/submit \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 10000000,
    "tenureMonths": 12,
    "purpose": "Home renovation"
  }'
```

## Entity Relationships

```
User (1) ----< (N) Loan
User (N) ----< (N) Role

Loan Entity:
- id
- user (User)
- amount
- tenureMonths
- purpose
- status (SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, DISBURSED)
- reviewNotes, approvalNotes, disbursementNotes
- timestamps (submittedAt, reviewedAt, approvedAt, disbursedAt)
- actors (reviewedBy, approvedBy, disbursedBy)
```

## Configuration

### application.properties / application.yml

```yaml
app:
  jwt:
    secret: your-secret-key-here-minimum-32-characters
    expiration: 3600000 # 1 hour in milliseconds
```

## Notes

- ADMIN role memiliki akses ke semua endpoints
- Setiap role hanya dapat melakukan action sesuai dengan statusnya dalam workflow
- Token JWT expire setelah 1 jam (configurable)
- Password di-hash menggunakan BCrypt
- Roles di-store di JWT token untuk stateless authentication
