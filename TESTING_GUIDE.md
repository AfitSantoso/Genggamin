# Panduan Testing API Genggamin

## Prerequisites

- Aplikasi berjalan di `http://localhost:8080`
- Database sudah terisi dengan roles (CUSTOMER, MARKETING, BRANCH_MANAGER, BACK_OFFICE)
- Gunakan Postman atau tool API testing lainnya

---

## 1️⃣ TEST ROLES (Tidak perlu Auth)

### GET All Roles

```
GET http://localhost:8080/roles
```

**Expected Response:**

```json
[
  {
    "id": 1,
    "name": "CUSTOMER",
    "description": "Customer aplikasi"
  },
  {
    "id": 2,
    "name": "MARKETING",
    "description": "Marketing"
  },
  {
    "id": 3,
    "name": "BRANCH_MANAGER",
    "description": "Branch Manager"
  },
  {
    "id": 4,
    "name": "BACK_OFFICE",
    "description": "Back Office"
  }
]
```

**✅ Status Code:** 200 OK

---

## 2️⃣ TEST REGISTER USER

### Register Customer

```
POST http://localhost:8080/auth/register
Content-Type: application/json

{
    "username": "customer1",
    "email": "customer1@example.com",
    "password": "password123",
    "phone": "08123456789",
    "isActive": true,
    "roles": ["CUSTOMER"]
}
```

**Expected Response:**

```json
{
  "message": "User created",
  "id": 1
}
```

**✅ Status Code:** 201 Created

---

### Register Marketing

```
POST http://localhost:8080/auth/register
Content-Type: application/json

{
    "username": "marketing1",
    "email": "marketing1@example.com",
    "password": "password123",
    "phone": "08123456790",
    "isActive": true,
    "roles": ["MARKETING"]
}
```

**Expected Response:**

```json
{
  "message": "User created",
  "id": 2
}
```

**✅ Status Code:** 201 Created

---

### Register Branch Manager

```
POST http://localhost:8080/auth/register
Content-Type: application/json

{
    "username": "manager1",
    "email": "manager1@example.com",
    "password": "password123",
    "phone": "08123456791",
    "isActive": true,
    "roles": ["BRANCH_MANAGER"]
}
```

**Expected Response:**

```json
{
  "message": "User created",
  "id": 3
}
```

**✅ Status Code:** 201 Created

---

### Register Back Office

```
POST http://localhost:8080/auth/register
Content-Type: application/json

{
    "username": "backoffice1",
    "email": "backoffice1@example.com",
    "password": "password123",
    "phone": "08123456792",
    "isActive": true,
    "roles": ["BACK_OFFICE"]
}
```

**Expected Response:**

```json
{
  "message": "User created",
  "id": 4
}
```

**✅ Status Code:** 201 Created

---

## 3️⃣ TEST LOGIN

### Login as Customer

```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "customer1",
    "password": "password123"
}
```

**Expected Response:**

```json
{
  "username": "customer1",
  "email": "customer1@example.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "roles": ["CUSTOMER"]
}
```

**✅ Status Code:** 200 OK

**📝 Simpan token untuk request selanjutnya!**

---

### Login as Marketing

```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "marketing1",
    "password": "password123"
}
```

**Expected Response:**

```json
{
  "username": "marketing1",
  "email": "marketing1@example.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "roles": ["MARKETING"]
}
```

**✅ Status Code:** 200 OK

---

### Login as Branch Manager

```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "manager1",
    "password": "password123"
}
```

**Expected Response:**

```json
{
  "username": "manager1",
  "email": "manager1@example.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "roles": ["BRANCH_MANAGER"]
}
```

**✅ Status Code:** 200 OK

---

### Login as Back Office

```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "backoffice1",
    "password": "password123"
}
```

**Expected Response:**

```json
{
  "username": "backoffice1",
  "email": "backoffice1@example.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "roles": ["BACK_OFFICE"]
}
```

**✅ Status Code:** 200 OK

---

## 4️⃣ TEST GET ALL USERS (Perlu Token)

```
GET http://localhost:8080/users
Authorization: Bearer {token_dari_login}
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": [
    {
      "id": 1,
      "username": "customer1",
      "email": "customer1@example.com",
      "phone": "08123456789",
      "isActive": true,
      "roles": [
        {
          "id": 1,
          "name": "CUSTOMER",
          "description": "Customer aplikasi"
        }
      ]
    },
    {
      "id": 2,
      "username": "marketing1",
      "email": "marketing1@example.com",
      "phone": "08123456790",
      "isActive": true,
      "roles": [
        {
          "id": 2,
          "name": "MARKETING",
          "description": "Marketing"
        }
      ]
    }
    // ... users lainnya
  ]
}
```

**✅ Status Code:** 200 OK

---

## 5️⃣ TEST LOAN - Submit (Customer)

**⚠️ Gunakan token dari login Customer!**

```
POST http://localhost:8080/loans/submit
Authorization: Bearer {customer_token}
Content-Type: application/json

{
    "amount": 50000000,
    "tenureMonths": 12,
    "purpose": "Modal usaha"
}
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Loan submitted successfully",
  "data": {
    "id": 1,
    "username": "customer1",
    "amount": 50000000,
    "tenureMonths": 12,
    "purpose": "Modal usaha",
    "status": "SUBMITTED",
    "submittedAt": "2025-12-22T10:30:00",
    "reviewNotes": null,
    "approvalNotes": null,
    "disbursementNotes": null
  }
}
```

**✅ Status Code:** 200 OK

---

## 6️⃣ TEST LOAN - Get My Loans (Customer)

**⚠️ Gunakan token dari login Customer!**

```
GET http://localhost:8080/loans/my-loans
Authorization: Bearer {customer_token}
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Loans retrieved successfully",
  "data": [
    {
      "id": 1,
      "username": "customer1",
      "amount": 50000000,
      "tenureMonths": 12,
      "purpose": "Modal usaha",
      "status": "SUBMITTED",
      "submittedAt": "2025-12-22T10:30:00",
      "reviewNotes": null,
      "approvalNotes": null,
      "disbursementNotes": null
    }
  ]
}
```

**✅ Status Code:** 200 OK

---

## 7️⃣ TEST LOAN - Get for Review (Marketing)

**⚠️ Gunakan token dari login Marketing!**

```
GET http://localhost:8080/loans/review
Authorization: Bearer {marketing_token}
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Loans retrieved successfully",
  "data": [
    {
      "id": 1,
      "username": "customer1",
      "amount": 50000000,
      "tenureMonths": 12,
      "purpose": "Modal usaha",
      "status": "SUBMITTED",
      "submittedAt": "2025-12-22T10:30:00",
      "reviewNotes": null,
      "approvalNotes": null,
      "disbursementNotes": null
    }
  ]
}
```

**✅ Status Code:** 200 OK

---

## 8️⃣ TEST LOAN - Review Loan (Marketing)

**⚠️ Gunakan token dari login Marketing!**

```
POST http://localhost:8080/loans/review/1
Authorization: Bearer {marketing_token}
Content-Type: application/json

{
    "approved": true,
    "notes": "Dokumen lengkap, disetujui untuk approval"
}
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Loan reviewed successfully",
  "data": {
    "id": 1,
    "username": "customer1",
    "amount": 50000000,
    "tenureMonths": 12,
    "purpose": "Modal usaha",
    "status": "UNDER_REVIEW",
    "submittedAt": "2025-12-22T10:30:00",
    "reviewNotes": "Dokumen lengkap, disetujui untuk approval",
    "reviewedAt": "2025-12-22T10:35:00",
    "reviewedBy": "marketing1",
    "approvalNotes": null,
    "disbursementNotes": null
  }
}
```

**✅ Status Code:** 200 OK

---

## 9️⃣ TEST LOAN - Get for Approval (Branch Manager)

**⚠️ Gunakan token dari login Branch Manager!**

```
GET http://localhost:8080/loans/approve
Authorization: Bearer {manager_token}
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Loans retrieved successfully",
  "data": [
    {
      "id": 1,
      "username": "customer1",
      "amount": 50000000,
      "tenureMonths": 12,
      "purpose": "Modal usaha",
      "status": "UNDER_REVIEW",
      "submittedAt": "2025-12-22T10:30:00",
      "reviewNotes": "Dokumen lengkap, disetujui untuk approval",
      "reviewedAt": "2025-12-22T10:35:00",
      "reviewedBy": "marketing1",
      "approvalNotes": null,
      "disbursementNotes": null
    }
  ]
}
```

**✅ Status Code:** 200 OK

---

## 🔟 TEST LOAN - Approve Loan (Branch Manager)

**⚠️ Gunakan token dari login Branch Manager!**

```
POST http://localhost:8080/loans/approve/1
Authorization: Bearer {manager_token}
Content-Type: application/json

{
    "approved": true,
    "notes": "Loan approved, ready for disbursement"
}
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Loan approved successfully",
  "data": {
    "id": 1,
    "username": "customer1",
    "amount": 50000000,
    "tenureMonths": 12,
    "purpose": "Modal usaha",
    "status": "APPROVED",
    "submittedAt": "2025-12-22T10:30:00",
    "reviewNotes": "Dokumen lengkap, disetujui untuk approval",
    "reviewedAt": "2025-12-22T10:35:00",
    "reviewedBy": "marketing1",
    "approvalNotes": "Loan approved, ready for disbursement",
    "approvedAt": "2025-12-22T10:40:00",
    "approvedBy": "manager1",
    "disbursementNotes": null
  }
}
```

**✅ Status Code:** 200 OK

---

## 1️⃣1️⃣ TEST LOAN - Get for Disbursement (Back Office)

**⚠️ Gunakan token dari login Back Office!**

```
GET http://localhost:8080/loans/disburse
Authorization: Bearer {backoffice_token}
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Loans retrieved successfully",
  "data": [
    {
      "id": 1,
      "username": "customer1",
      "amount": 50000000,
      "tenureMonths": 12,
      "purpose": "Modal usaha",
      "status": "APPROVED",
      "submittedAt": "2025-12-22T10:30:00",
      "reviewNotes": "Dokumen lengkap, disetujui untuk approval",
      "reviewedAt": "2025-12-22T10:35:00",
      "reviewedBy": "marketing1",
      "approvalNotes": "Loan approved, ready for disbursement",
      "approvedAt": "2025-12-22T10:40:00",
      "approvedBy": "manager1",
      "disbursementNotes": null
    }
  ]
}
```

**✅ Status Code:** 200 OK

---

## 1️⃣2️⃣ TEST LOAN - Disburse Loan (Back Office)

**⚠️ Gunakan token dari login Back Office!**

```
POST http://localhost:8080/loans/disburse/1
Authorization: Bearer {backoffice_token}
Content-Type: application/json

{
    "approved": true,
    "notes": "Dana telah ditransfer ke rekening customer"
}
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Loan disbursed successfully",
  "data": {
    "id": 1,
    "username": "customer1",
    "amount": 50000000,
    "tenureMonths": 12,
    "purpose": "Modal usaha",
    "status": "DISBURSED",
    "submittedAt": "2025-12-22T10:30:00",
    "reviewNotes": "Dokumen lengkap, disetujui untuk approval",
    "reviewedAt": "2025-12-22T10:35:00",
    "reviewedBy": "marketing1",
    "approvalNotes": "Loan approved, ready for disbursement",
    "approvedAt": "2025-12-22T10:40:00",
    "approvedBy": "manager1",
    "disbursementNotes": "Dana telah ditransfer ke rekening customer",
    "disbursedAt": "2025-12-22T10:45:00",
    "disbursedBy": "backoffice1"
  }
}
```

**✅ Status Code:** 200 OK

---

## ❌ TEST ERROR CASES

### 1. Login dengan password salah

```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "customer1",
    "password": "wrongpassword"
}
```

**Expected Response:**

```json
{
  "message": "Invalid credentials"
}
```

**✅ Status Code:** 401 Unauthorized

---

### 2. Akses endpoint tanpa token

```
GET http://localhost:8080/loans/my-loans
(Tanpa Authorization header)
```

**Expected Response:**

```json
{
  "message": "Full authentication is required"
}
```

**✅ Status Code:** 401 Unauthorized

---

### 3. Akses endpoint dengan role yang salah

```
GET http://localhost:8080/loans/review
Authorization: Bearer {customer_token}
(Customer mencoba akses endpoint Marketing)
```

**Expected Response:**

```json
{
  "message": "Access Denied"
}
```

**✅ Status Code:** 403 Forbidden

---

## 📊 TESTING CHECKLIST

### Authentication & Authorization

- [ ] ✅ GET /roles - berhasil dapat list roles
- [ ] ✅ POST /auth/register (Customer) - berhasil create user
- [ ] ✅ POST /auth/register (Marketing) - berhasil create user
- [ ] ✅ POST /auth/register (Branch Manager) - berhasil create user
- [ ] ✅ POST /auth/register (Back Office) - berhasil create user
- [ ] ✅ POST /auth/login (Customer) - dapat token
- [ ] ✅ POST /auth/login (Marketing) - dapat token
- [ ] ✅ POST /auth/login (Branch Manager) - dapat token
- [ ] ✅ POST /auth/login (Back Office) - dapat token

### User Management

- [ ] ✅ GET /users - berhasil dapat list users dengan token

### Loan Flow (CUSTOMER)

- [ ] ✅ POST /loans/submit - customer bisa submit loan
- [ ] ✅ GET /loans/my-loans - customer bisa lihat loan sendiri

### Loan Flow (MARKETING)

- [ ] ✅ GET /loans/review - marketing bisa lihat loans yang perlu direview
- [ ] ✅ POST /loans/review/{id} - marketing bisa review loan

### Loan Flow (BRANCH MANAGER)

- [ ] ✅ GET /loans/approve - manager bisa lihat loans yang perlu diapprove
- [ ] ✅ POST /loans/approve/{id} - manager bisa approve/reject loan

### Loan Flow (BACK OFFICE)

- [ ] ✅ GET /loans/disburse - back office bisa lihat loans yang perlu disburse
- [ ] ✅ POST /loans/disburse/{id} - back office bisa disburse loan

### Error Handling

- [ ] ✅ Login dengan password salah - return 401
- [ ] ✅ Akses endpoint tanpa token - return 401
- [ ] ✅ Akses endpoint dengan role salah - return 403

---

## 🔥 TIPS TESTING

1. **Urutan Testing:** Ikuti urutan dari atas ke bawah
2. **Simpan Token:** Setiap kali login, simpan token untuk request berikutnya
3. **Check Status:** Pastikan setiap request return status code yang benar
4. **Loan Flow:** Test satu loan dari submit sampai disburse secara lengkap
5. **Role Testing:** Test setiap role untuk memastikan authorization bekerja

---

## 📝 NOTES

- Semua endpoint kecuali `/auth/*` dan `/roles` memerlukan token JWT
- Token didapat dari endpoint `/auth/login`
- Token harus dimasukkan di header: `Authorization: Bearer {token}`
- Status loan berurutan: SUBMITTED → UNDER_REVIEW → APPROVED → DISBURSED
- Jika reject di marketing/manager, status jadi REJECTED

---

**🎉 Good Luck Testing!**
