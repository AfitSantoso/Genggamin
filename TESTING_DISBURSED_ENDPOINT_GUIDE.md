# Testing Endpoint /loans/disbursed - Troubleshooting Guide

## 📋 Endpoint Information

**Endpoint:** `GET /loans/disbursed`  
**Method:** GET  
**Authentication:** Required (Bearer Token)  
**Roles:** BACK_OFFICE, ADMIN  
**Purpose:** Mendapatkan semua loans yang sudah dicairkan dengan detail disbursement (JOIN data)

---

## 🔧 Prerequisites Checklist

### 1. Database Setup

Pastikan tabel `loan_disbursements` sudah ada dengan kolom yang lengkap:

```sql
-- Run this query to check table structure
USE LoanDB;
SELECT
    c.name AS ColumnName,
    t.name AS DataType
FROM sys.columns c
INNER JOIN sys.types t ON c.user_type_id = t.user_type_id
WHERE c.object_id = OBJECT_ID('loan_disbursements')
ORDER BY c.column_id;
```

**Expected columns:**

- id (bigint)
- loan_id (bigint)
- disbursed_by (bigint)
- disbursement_amount (decimal)
- disbursement_date (datetime2)
- bank_account (varchar)
- status (varchar)
- notes (varchar) ← **IMPORTANT: Must exist**

**If notes column is missing, run:**

```bash
sqlcmd -S localhost -U bot -P "P@ssw0rd" -d LoanDB -i ALTER_LOAN_DISBURSEMENTS_ADD_NOTES.sql
```

### 2. Application Running

Pastikan aplikasi Spring Boot sudah running:

```bash
# Check if application is running on port 8080
curl http://localhost:8080/actuator/health
# OR
netstat -ano | findstr :8080
```

**If not running, restart application:**

- In VS Code: Stop the running terminal and restart Spring Boot application
- Or run: `./mvnw spring-boot:run`

### 3. Test Data

Pastikan ada loan yang sudah DISBURSED:

```sql
-- Check if there are disbursed loans
USE LoanDB;
SELECT l.id, l.status, ld.id AS disbursement_id
FROM loans l
LEFT JOIN loan_disbursements ld ON l.id = ld.loan_id
WHERE l.status = 'DISBURSED';
```

**If no disbursed loans exist, create one following the workflow:**

1. Customer submit loan → SUBMITTED
2. Marketing review → UNDER_REVIEW
3. Branch Manager approve → APPROVED
4. Back Office disburse → DISBURSED

---

## 🔐 Authentication Issues

### Issue: "401 Unauthorized" or "Invalid Token"

**Causes:**

1. Token expired (default JWT expiration)
2. Token tidak valid
3. Token tidak diinclude di header

**Solutions:**

#### Solution 1: Login ulang untuk mendapatkan token baru

```bash
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "backoffice1",
  "password": "password123"
}
```

**Save the token from response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "backoffice1",
  "roles": ["ROLE_BACK_OFFICE"]
}
```

#### Solution 2: Check token in request header

Make sure you're using the correct format:

**CORRECT:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**WRONG:**

```
Authorization: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...  ← Missing "Bearer "
Authorization: Token eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...  ← Wrong prefix
```

#### Solution 3: Check if user has correct role

Run this query to verify user role:

```sql
SELECT u.username, r.name AS role_name
FROM users u
INNER JOIN user_roles ur ON u.id = ur.user_id
INNER JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'backoffice1';
```

Expected result: User should have `ROLE_BACK_OFFICE` or `ROLE_ADMIN`

---

## 📝 Testing Steps

### Step 1: Get Fresh Token

**Request:**

```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "backoffice1",
  "password": "password123"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJiYWNrb2ZmaWNlMSIsInJvbGVzIjpbIlJPTEVfQkFDS19PRkZJQ0UiXSwiaWF0IjoxNzM2MzA0MDAwLCJleHAiOjE3MzYzOTA0MDB9.xxxxx",
    "username": "backoffice1",
    "roles": ["ROLE_BACK_OFFICE"]
  }
}
```

**⚠️ COPY THE TOKEN!**

### Step 2: Test Endpoint

**Request:**

```http
GET http://localhost:8080/loans/disbursed
Authorization: Bearer <YOUR_TOKEN_HERE>
```

**Expected Response (Success - 200 OK):**

```json
{
  "success": true,
  "message": "Disbursed loans retrieved successfully",
  "data": [
    {
      "id": 1,
      "customerId": 5,
      "plafondId": 1,
      "loanAmount": 9000000.0,
      "tenorMonth": 6,
      "interestRate": 12.5,
      "purpose": null,
      "status": "DISBURSED",
      "submissionDate": "2026-01-06T10:30:00",
      "createdAt": "2026-01-06T10:30:00",
      "updatedAt": "2026-01-06T16:00:00",
      "disbursementId": 1,
      "disbursedBy": 6,
      "disbursementAmount": 9000000.0,
      "disbursementDate": "2026-01-06T16:00:00",
      "bankAccount": "1234567890123",
      "disbursementStatus": "COMPLETED"
    }
  ]
}
```

**Expected Response (Empty - 200 OK):**

```json
{
  "success": true,
  "message": "Disbursed loans retrieved successfully",
  "data": []
}
```

This is normal if there are no disbursed loans yet.

---

## 🚨 Common Errors & Solutions

### Error 1: 401 Unauthorized

```json
{
  "success": false,
  "message": "Unauthorized"
}
```

**Fix:** Login ulang dan gunakan token yang baru (lihat Step 1)

### Error 2: 403 Forbidden

```json
{
  "success": false,
  "message": "Access Denied"
}
```

**Fix:** User tidak memiliki role BACK_OFFICE atau ADMIN. Login dengan user yang benar.

### Error 3: 500 Internal Server Error

```json
{
  "success": false,
  "message": "Could not execute statement..."
}
```

**Possible Causes:**

1. Column `notes` tidak ada di tabel `loan_disbursements`
2. Tabel `loan_disbursements` tidak ada

**Fix:**

```bash
# Run the ALTER script
sqlcmd -S localhost -U bot -P "P@ssw0rd" -d LoanDB -i ALTER_LOAN_DISBURSEMENTS_ADD_NOTES.sql

# Then restart Spring Boot application
```

### Error 4: Connection Refused

```
Could not connect to localhost:8080
```

**Fix:** Aplikasi tidak running. Start Spring Boot application.

---

## 🧪 Postman Collection Example

### Environment Variables

```
baseUrl = http://localhost:8080
backofficeToken = <token dari login>
```

### Request Setup

**1. Login Request**

- Method: POST
- URL: `{{baseUrl}}/auth/login`
- Body (JSON):

```json
{
  "username": "backoffice1",
  "password": "password123"
}
```

- Test Script (to auto-save token):

```javascript
var jsonData = pm.response.json();
if (jsonData.success && jsonData.data && jsonData.data.token) {
  pm.environment.set("backofficeToken", jsonData.data.token);
  console.log("Token saved:", jsonData.data.token);
}
```

**2. Get Disbursed Loans Request**

- Method: GET
- URL: `{{baseUrl}}/loans/disbursed`
- Headers:
  - Key: `Authorization`
  - Value: `Bearer {{backofficeToken}}`
- Test Script (validation):

```javascript
pm.test("Status code is 200", function () {
  pm.response.to.have.status(200);
});

pm.test("Response has success field", function () {
  var jsonData = pm.response.json();
  pm.expect(jsonData).to.have.property("success");
  pm.expect(jsonData.success).to.be.true;
});

pm.test("Response has data array", function () {
  var jsonData = pm.response.json();
  pm.expect(jsonData).to.have.property("data");
  pm.expect(jsonData.data).to.be.an("array");
});
```

---

## ✅ Verification Checklist

Before testing, verify:

- [ ] Database LoanDB exists and is accessible
- [ ] Table `loan_disbursements` exists with all columns (including `notes`)
- [ ] Spring Boot application is running on port 8080
- [ ] At least one loan has status DISBURSED (for non-empty response)
- [ ] User `backoffice1` exists and has role ROLE_BACK_OFFICE
- [ ] Fresh JWT token obtained from login endpoint
- [ ] Token is included in Authorization header with "Bearer " prefix

---

## 🔍 Debug Tips

### Check Application Logs

Look for errors in Spring Boot console:

```
ERROR: Could not commit JPA transaction
ERROR: Column 'notes' not found
ERROR: Invalid token
```

### Test with curl

```bash
# Get token first
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"backoffice1","password":"password123"}'

# Use the token from response
curl -X GET http://localhost:8080/loans/disbursed \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Check Database Directly

```sql
-- Verify data exists
SELECT * FROM loan_disbursements;

-- Test the JOIN query
SELECT l.*, ld.*
FROM loans l
INNER JOIN loan_disbursements ld ON l.id = ld.loan_id
WHERE l.status = 'DISBURSED';
```

---

## 📞 Need Help?

If masih error setelah semua langkah di atas:

1. Check Spring Boot console logs untuk error detail
2. Verify tabel structure dengan TEST_DISBURSEMENT_ENDPOINT.sql
3. Test endpoint dengan curl untuk isolate masalah Postman
4. Restart application setelah perubahan database

---

**Good luck with testing! 🚀**
