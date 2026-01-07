# Testing Workflow Loan Approval - Postman Guide

Panduan lengkap untuk testing workflow proses pengajuan pinjaman dari awal hingga akhir.

---

## Prerequisites

Sebelum memulai testing, pastikan:

1. Aplikasi sudah running
2. Database sudah berisi data plafond (jalankan init-db.sql)
3. Siapkan 4 user dengan role berbeda:
   - User dengan role CUSTOMER
   - User dengan role MARKETING
   - User dengan role BRANCH_MANAGER
   - User dengan role BACK_OFFICE

---

## Step 0: Persiapan - Login untuk Setiap Role

### 0.1 Login sebagai CUSTOMER

**POST** `http://localhost:8080/auth/login`

```json
{
  "username": "customer1",
  "password": "password123"
}
```

**Simpan token** dari response untuk digunakan di step selanjutnya:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "customer1",
  "roles": ["ROLE_CUSTOMER"]
}
```

### 0.2 Login sebagai MARKETING

**POST** `http://localhost:8080/auth/login`

```json
{
  "username": "marketing1",
  "password": "password123"
}
```

### 0.3 Login sebagai BRANCH_MANAGER

**POST** `http://localhost:8080/auth/login`

```json
{
  "username": "manager1",
  "password": "password123"
}
```

### 0.4 Login sebagai BACK_OFFICE

**POST** `http://localhost:8080/auth/login`

```json
{
  "username": "backoffice1",
  "password": "password123"
}
```

---

## Step 1: Pastikan Customer Memiliki Profil Lengkap

### 1.1 Cek apakah customer sudah punya profil

**GET** `http://localhost:8080/customers/has-profile`

**Headers:**

```
Authorization: Bearer <CUSTOMER_TOKEN>
```

### 1.2 Jika belum ada, buat profil customer

**POST** `http://localhost:8080/customers/profile`

**Headers:**

```
Authorization: Bearer <CUSTOMER_TOKEN>
Content-Type: application/json
```

**Body:**

```json
{
  "nik": "3201234567890123",
  "fullName": "John Doe Customer",
  "dateOfBirth": "1990-05-15",
  "placeOfBirth": "Jakarta",
  "address": "Jl. Sudirman No. 123, Jakarta Pusat",
  "phone": "081234567890",
  "email": "customer1@example.com",
  "monthlyIncome": 15000000,
  "occupation": "Software Engineer",
  "emergencyContact": {
    "name": "Jane Doe",
    "relationship": "Sister",
    "phone": "081234567891"
  }
}
```

---

## Step 2: Cek Plafond yang Tersedia

### 2.1 Lihat semua plafond aktif

**GET** `http://localhost:8080/plafonds/active`

**Response Example:**

```json
[
  {
    "id": 1,
    "name": "Plafond Bronze",
    "minIncome": 5000000,
    "maxIncome": 10000000,
    "maxLoanAmount": 50000000,
    "interestRate": 12.5,
    "maxTenor": 24,
    "description": "Plafond untuk income 5-10 juta",
    "isActive": true
  },
  {
    "id": 2,
    "name": "Plafond Silver",
    "minIncome": 10000001,
    "maxIncome": 20000000,
    "maxLoanAmount": 100000000,
    "interestRate": 11.0,
    "maxTenor": 36,
    "description": "Plafond untuk income 10-20 juta",
    "isActive": true
  }
]
```

### 2.2 Cek plafond berdasarkan income customer

**GET** `http://localhost:8080/plafonds/by-income/15000000`

---

## Step 3: CUSTOMER Submit Loan (Status: SUBMITTED)

**POST** `http://localhost:8080/loans/submit`

**Headers:**

```
Authorization: Bearer <CUSTOMER_TOKEN>
Content-Type: application/json
```

**Body:**

```json
{
  "amount": 9000000,
  "tenureMonths": 6,
  "purpose": "beli ferrari"
}
```

**📋 Catatan Penting:**

- `amount`: Jumlah pinjaman (BigDecimal) - akan divalidasi ≤ maxAmount dari plafond
- `tenureMonths`: Jangka waktu dalam bulan (Long) - akan divalidasi ≤ tenorMonth dari plafond
- `purpose`: Tujuan pinjaman (String) - **TIDAK disimpan ke database** (field @Transient)
- `plafondId`: **OTOMATIS** dipilih sistem berdasarkan customer monthlyIncome
- `interestRate`: **OTOMATIS** diambil dari plafond yang dipilih
- Contoh: Customer dengan income Rp 5.000.000 akan mendapat Plafond Bronze (id=1, interestRate=12.5%)

**Response:**

```json
{
  "id": 1,
  "customerId": 5,
  "plafondId": 1,
  "loanAmount": 9000000.0,
  "tenorMonth": 6,
  "interestRate": 12.5,
  "purpose": "beli ferrari",
  "status": "SUBMITTED",
  "submissionDate": "2026-01-06T10:30:00",
  "createdAt": "2026-01-06T10:30:00",
  "updatedAt": null
}
```

**💡 Perhatikan:**

- Response menggunakan `tenorMonth` sesuai dengan database
- `plafondId: 1` otomatis dipilih sistem berdasarkan customer income
- `interestRate: 12.5` otomatis diambil dari Plafond Bronze
- `customerId` adalah foreign key ke tabel customers
- Sistem akan validasi loanAmount ≤ maxAmount dan tenorMonth ≤ tenorMonth dari plafond

**⚠️ Simpan `loanId` dari response untuk step selanjutnya!**

---

## Step 4: Customer Cek Status Loan

**GET** `http://localhost:8080/loans/my-loans`

**Headers:**

```
Authorization: Bearer <CUSTOMER_TOKEN>
```

Atau cek loan spesifik:

**GET** `http://localhost:8080/loans/1`

**Headers:**

```
Authorization: Bearer <CUSTOMER_TOKEN>
```

---

## Step 5: MARKETING Review Loan (Status: REVIEWED atau REJECTED)

### 5.1 Marketing cek loan yang perlu direview (Belum direview)

**GET** `http://localhost:8080/loans/review`

**Headers:**

```
Authorization: Bearer <MARKETING_TOKEN>
```

**Response:**

```json
[
  {
    "id": 1,
    "customerId": 5,
    "plafondId": 1,
    "loanAmount": 9000000.0,
    "tenorMonth": 6,
    "interestRate": 12.5,
    "purpose": "beli ferrari",
    "status": "SUBMITTED",
    "submissionDate": "2026-01-06T10:30:00",
    "createdAt": "2026-01-06T10:30:00",
    "updatedAt": null
  }
]
```

### 5.1.1 Marketing cek loan yang sudah direview (Dengan data review)

**GET** `http://localhost:8080/loans/reviewed`

**Headers:**

```
Authorization: Bearer <MARKETING_TOKEN>
```

**Response:**

```json
[
  {
    "id": 1,
    "customerId": 5,
    "plafondId": 1,
    "loanAmount": 9000000.0,
    "tenorMonth": 6,
    "interestRate": 12.5,
    "purpose": "beli ferrari",
    "status": "UNDER_REVIEW",
    "submissionDate": "2026-01-06T10:30:00",
    "createdAt": "2026-01-06T10:30:00",
    "updatedAt": "2026-01-06T11:00:00",
    "reviewId": 1,
    "reviewedBy": 3,
    "reviewNotes": "Dokumen lengkap, profil customer baik",
    "reviewStatus": "APPROVED",
    "reviewedAt": "2026-01-06T11:00:00"
  }
]
```

**📋 Catatan:**

- Endpoint ini menampilkan loans yang **sudah direview** dengan **JOIN** ke tabel `loan_reviews`
- Response include data dari:
  - **Tabel loans**: id, customerId, plafondId, loanAmount, tenorMonth, status, dll
  - **Tabel loan_reviews**: reviewId, reviewedBy, reviewNotes, reviewStatus, reviewedAt
- `reviewedBy` adalah user_id dari marketing yang melakukan review
- `reviewStatus` bisa "APPROVED" atau "REJECTED"

### 5.2 Marketing melakukan review (APPROVE)

**POST** `http://localhost:8080/loans/review/1`

**Headers:**

```
Authorization: Bearer <MARKETING_TOKEN>
Content-Type: application/json
```

**Body (APPROVE):**

```json
{
  "action": "APPROVE",
  "notes": "Dokumen lengkap, profil customer baik, income sesuai. Direkomendasikan untuk approval."
}
```

**Body Alternative (REJECT):**

```json
{
  "action": "REJECT",
  "notes": "Dokumen tidak lengkap, riwayat kredit bermasalah."
}
```

**📋 Catatan:**

- Field `notes` akan disimpan di tabel `loan_reviews` (bukan di tabel `loans`)
- Tabel `loan_reviews` menyimpan: `loan_id`, `reviewed_by`, `review_notes`, `review_status`, `reviewed_at`
- `review_status` akan berisi "APPROVED" atau "REJECTED" sesuai dengan action

**Response (APPROVE):**

```json
{
  "id": 1,
  "customerId": 5,
  "plafondId": 1,
  "loanAmount": 9000000.0,
  "tenorMonth": 6,
  "interestRate": 12.5,
  "purpose": "beli ferrari",
  "status": "UNDER_REVIEW",
  "submissionDate": "2026-01-06T10:30:00",
  "createdAt": "2026-01-06T10:30:00",
  "updatedAt": "2026-01-06T11:00:00"
}
```

---

## Step 6: BRANCH_MANAGER Approve Loan (Status: APPROVED atau REJECTED)

### 6.1 Branch Manager cek loan yang perlu approval (Belum di-approve)

**GET** `http://localhost:8080/loans/approve`

**Headers:**

```
Authorization: Bearer <BRANCH_MANAGER_TOKEN>
```

**Response:**

```json
[
  {
    "id": 1,
    "customerId": 5,
    "plafondId": 1,
    "loanAmount": 9000000.0,
    "tenorMonth": 6,
    "interestRate": 12.5,
    "purpose": "beli ferrari",
    "status": "UNDER_REVIEW",
    "submissionDate": "2026-01-06T10:30:00",
    "createdAt": "2026-01-06T10:30:00",
    "updatedAt": "2026-01-06T11:00:00"
  }
]
```

### 6.1.1 Branch Manager cek loan yang sudah di-approve (Dengan data approval)

**GET** `http://localhost:8080/loans/approved`

**Headers:**

```
Authorization: Bearer <BRANCH_MANAGER_TOKEN>
```

**Response:**

```json
[
  {
    "id": 1,
    "customerId": 5,
    "plafondId": 1,
    "loanAmount": 9000000.0,
    "tenorMonth": 6,
    "interestRate": 12.5,
    "purpose": "beli ferrari",
    "status": "APPROVED",
    "submissionDate": "2026-01-06T10:30:00",
    "createdAt": "2026-01-06T10:30:00",
    "updatedAt": "2026-01-06T14:00:00",
    "approvalId": 1,
    "approvedBy": 4,
    "approvalStatus": "APPROVED",
    "approvalNotes": "Loan disetujui, dapat dilanjutkan ke pencairan",
    "approvedAt": "2026-01-06T14:00:00"
  }
]
```

**📋 Catatan:**

- Endpoint ini menampilkan loans yang **sudah di-approve/reject** dengan **JOIN** ke tabel `loan_approvals`
- Response include data dari:
  - **Tabel loans**: id, customerId, plafondId, loanAmount, tenorMonth, status, dll
  - **Tabel loan_approvals**: approvalId, approvedBy, approvalStatus, approvalNotes, approvedAt
- `approvedBy` adalah user_id dari branch manager yang melakukan approval
- `approvalStatus` bisa "APPROVED" atau "REJECTED"

### 6.2 Branch Manager melakukan approval

**POST** `http://localhost:8080/loans/approve/1`

**Headers:**

```
Authorization: Bearer <BRANCH_MANAGER_TOKEN>
Content-Type: application/json
```

**Body (APPROVE):**

```json
{
  "approved": true,
  "notes": "Loan disetujui, dapat dilanjutkan ke pencairan. Amount dan tenor sesuai policy."
}
```

**Body Alternative (REJECT):**

```json
{
  "approved": false,
  "notes": "Loan ditolak karena tidak sesuai dengan risk appetite perusahaan."
}
```

**📋 Catatan:**

- Field `notes` akan disimpan di tabel `loan_approvals` (bukan di tabel `loans`)
- Tabel `loan_approvals` menyimpan: `loan_id`, `approved_by`, `approval_status`, `approval_notes`, `approved_at`
- `approval_status` akan berisi "APPROVED" atau "REJECTED" sesuai dengan field `approved` (true/false)

**Response (APPROVE):**

```json
{
  "id": 1,
  "customerId": 5,
  "plafondId": 1,
  "loanAmount": 9000000.0,
  "tenorMonth": 6,
  "interestRate": 12.5,
  "purpose": "beli ferrari",
  "status": "APPROVED",
  "submissionDate": "2026-01-06T10:30:00",
  "createdAt": "2026-01-06T10:30:00",
  "updatedAt": "2026-01-06T14:00:00"
}
```

---

## Step 7: BACK_OFFICE Disburse Loan (Status: DISBURSED)

### 7.1 Back Office cek loan yang siap dicairkan

**GET** `http://localhost:8080/loans/disburse`

**Headers:**

```
Authorization: Bearer <BACK_OFFICE_TOKEN>
```

**Response:**

```json
[
  {
    "id": 1,
    "customerId": 5,
    "plafondId": 1,
    "loanAmount": 9000000.0,
    "tenorMonth": 6,
    "interestRate": 12.5,
    "purpose": "beli ferrari",
    "status": "APPROVED",
    "submissionDate": "2026-01-06T10:30:00",
    "createdAt": "2026-01-06T10:30:00",
    "updatedAt": "2026-01-06T14:00:00"
  }
]
```

### 7.2 Back Office melakukan pencairan

**POST** `http://localhost:8080/loans/disburse/1`

**Headers:**

```
Authorization: Bearer <BACK_OFFICE_TOKEN>
Content-Type: application/json
```

**Body:**

```json
{
  "action": "DISBURSE",
  "notes": "Dana telah ditransfer ke rekening customer xxxxxxxxx123 sebesar Rp 75.000.000. Nomor referensi: TRX20260106001."
}
```

**Response:**

```json
{
  "id": 1,
  "customerId": 5,
  "plafondId": 1,
  "loanAmount": 9000000.0,
  "tenorMonth": 6,
  "interestRate": 12.5,
  "purpose": "beli ferrari",
  "status": "DISBURSED",
  "submissionDate": "2026-01-06T10:30:00",
  "createdAt": "2026-01-06T10:30:00",
  "updatedAt": "2026-01-06T16:00:00"
}
```

---

## Step 8: Verifikasi Final

### 8.1 Customer cek status akhir loan

**GET** `http://localhost:8080/loans/my-loans`

**Headers:**

```
Authorization: Bearer <CUSTOMER_TOKEN>
```

**Response:**

```json
[
  {
    "id": 1,
    "customerId": 5,
    "plafondId": 1,
    "loanAmount": 9000000.0,
    "tenorMonth": 6,
    "interestRate": 12.5,
    "purpose": "beli ferrari",
    "status": "DISBURSED",
    "submissionDate": "2026-01-06T10:30:00",
    "createdAt": "2026-01-06T10:30:00",
    "updatedAt": "2026-01-06T16:00:00"
  }
]
```

---

## Skenario Testing Lengkap

### Skenario 1: Loan Approved (Happy Path)

```
✅ Customer submit → SUBMITTED
✅ Marketing approve → UNDER_REVIEW
✅ Branch Manager approve → APPROVED
✅ Back Office disburse → DISBURSED
```

### Skenario 2: Rejected oleh Marketing

```
✅ Customer submit → SUBMITTED
❌ Marketing reject → REJECTED
(Workflow berhenti di sini)
```

### Skenario 3: Rejected oleh Branch Manager

```
✅ Customer submit → SUBMITTED
✅ Marketing approve → UNDER_REVIEW
❌ Branch Manager reject → REJECTED
(Workflow berhenti di sini)
```

---

## Contoh Postman Collection Structure

```
📁 Genggamin Loan Workflow
│
├─ 📁 0. Authentication
│  ├─ Login as CUSTOMER
│  ├─ Login as MARKETING
│  ├─ Login as BRANCH_MANAGER
│  └─ Login as BACK_OFFICE
│
├─ 📁 1. Customer Profile Setup
│  ├─ Check Has Profile
│  ├─ Create Profile
│  └─ Get Profile
│
├─ 📁 2. Check Plafonds
│  ├─ Get All Active Plafonds
│  └─ Get Plafonds by Income
│
├─ 📁 3. Submit Loan (CUSTOMER)
│  ├─ Submit Loan
│  └─ Get My Loans
│
├─ 📁 4. Review Loan (MARKETING)
│  ├─ Get Loans to Review (SUBMITTED)
│  ├─ Get Reviewed Loans (with JOIN)
│  ├─ Approve Loan
│  └─ Reject Loan
│
├─ 📁 5. Approve Loan (BRANCH_MANAGER)
│  ├─ Get Loans to Approve
│  ├─ Approve Loan
│  └─ Reject Loan
│
├─ 📁 6. Disburse Loan (BACK_OFFICE)
│  ├─ Get Loans to Disburse
│  └─ Disburse Loan
│
└─ 📁 7. Verification
   ├─ Get Loan Detail
   └─ Get All Loans (ADMIN)
```

---

## Tips Testing

1. **Environment Variables di Postman**

   ```
   {{baseUrl}} = http://localhost:8080
   {{customerToken}} = <token dari login customer>
   {{marketingToken}} = <token dari login marketing>
   {{managerToken}} = <token dari login branch manager>
   {{backofficeToken}} = <token dari login backoffice>
   {{loanId}} = <id dari loan yang dibuat>
   ```

2. **Test Scripts di Postman**
   Untuk auto-save token setelah login:

   ```javascript
   // Di Tests tab pada request login
   var jsonData = pm.response.json();
   pm.environment.set("customerToken", jsonData.token);
   ```

3. **Validasi Response**

   - Status code harus 200 atau 201
   - Response body harus sesuai dengan expected format
   - Status loan harus berubah sesuai workflow

4. **Error Scenarios**

   - Test dengan token yang salah/expired
   - Test dengan role yang tidak sesuai
   - Test dengan data yang tidak valid

5. **Validasi Business Rules**
   - `loanAmount` harus decimal(18,2), sesuai dengan maxLoanAmount dari plafond customer
   - `tenorMonth` harus bigint, sesuai dengan maxTenor dari plafond customer
   - `purpose` adalah varchar(500) yang disimpan ke database
   - `plafondId` otomatis ditentukan sistem berdasarkan customer monthlyIncome
   - `interestRate` otomatis diambil dari plafond, tidak bisa diubah customer

---

## Common Errors & Solutions

### Error 401 Unauthorized

- **Cause**: Token tidak valid atau expired
- **Solution**: Login ulang dan update token

### Error 403 Forbidden

- **Cause**: User tidak punya role yang sesuai
- **Solution**: Pastikan menggunakan token dengan role yang benar

### Error 404 Not Found

- **Cause**: Loan ID tidak ditemukan
- **Solution**: Cek kembali loan ID yang digunakan

### Error 400 Bad Request

- **Cause**: Request body tidak valid
- **Solution**: Validasi format JSON dan field yang required

---

## Next Steps

Setelah testing workflow berhasil:

1. ✅ Test skenario rejection di setiap stage
2. ✅ Test dengan multiple loans
3. ✅ Test permission boundaries (akses tanpa role yang tepat)
4. ✅ Test dengan data edge cases (amount > maxLoanAmount, tenor > maxTenor)
5. ✅ Integration test dengan unit tests

---

**Happy Testing! 🚀**
