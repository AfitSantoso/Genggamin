# Customer Profile Management API Documentation

## Overview

API ini mengelola data profil customer yang diperlukan untuk pengajuan plafond dan pinjaman. Customer harus mengisi data ini setelah login untuk melengkapi persyaratan pendaftaran.

## Authentication

Semua endpoint memerlukan JWT token yang valid di header:

```
Authorization: Bearer <jwt_token>
```

## Endpoints

### 1. Create/Update Customer Profile

Endpoint untuk membuat atau memperbarui data profil customer yang sedang login.

**Endpoint:** `POST /customers/profile`

**Request Body:**

```json
{
  "nik": "3201234567890123",
  "address": "Jl. Contoh No. 123, Jakarta",
  "dateOfBirth": "1990-01-15",
  "monthlyIncome": 5000000.0,
  "emergencyContacts": [
    {
      "contactName": "Budi Santoso",
      "contactPhone": "081234567890",
      "relationship": "ORANG_TUA"
    },
    {
      "contactName": "Siti Rahayu",
      "contactPhone": "081234567891",
      "relationship": "SAUDARA"
    }
  ]
}
```

**Field Descriptions:**

- `nik` (String, required): Nomor Induk Kependudukan (16 digit)
- `address` (String, optional): Alamat lengkap customer
- `dateOfBirth` (Date, optional): Tanggal lahir (format: YYYY-MM-DD)
- `monthlyIncome` (Decimal, optional): Pendapatan bulanan
- `emergencyContacts` (Array, optional): Daftar kontak darurat
  - `contactName` (String, required): Nama kontak darurat
  - `contactPhone` (String, required): Nomor telepon kontak darurat
  - `relationship` (String, optional): Hubungan (ORANG_TUA, SAUDARA, PASANGAN, TEMAN)

**Success Response (200 OK):**

```json
{
  "success": true,
  "message": "Customer profile saved successfully",
  "data": {
    "id": 1,
    "userId": 10,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "nik": "3201234567890123",
    "address": "Jl. Contoh No. 123, Jakarta",
    "dateOfBirth": "1990-01-15",
    "monthlyIncome": 5000000.0,
    "emergencyContacts": [
      {
        "id": 1,
        "contactName": "Budi Santoso",
        "contactPhone": "081234567890",
        "relationship": "ORANG_TUA",
        "createdAt": "2025-12-26T10:30:00"
      },
      {
        "id": 2,
        "contactName": "Siti Rahayu",
        "contactPhone": "081234567891",
        "relationship": "SAUDARA",
        "createdAt": "2025-12-26T10:30:00"
      }
    ],
    "createdAt": "2025-12-26T10:30:00"
  }
}
```

**Error Response (400 Bad Request):**

```json
{
  "success": false,
  "message": "NIK already registered by another customer",
  "data": null
}
```

**Error Response (401 Unauthorized):**

```json
{
  "success": false,
  "message": "Unauthorized",
  "data": null
}
```

---

### 2. Get Current User Profile

Endpoint untuk mendapatkan data profil customer yang sedang login.

**Endpoint:** `GET /api/customers/profile`

**Success Response (200 OK):**

```json
{
  "success": true,
  "message": "Customer profile retrieved successfully",
  "data": {
    "id": 1,
    "userId": 10,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "nik": "3201234567890123",
    "address": "Jl. Contoh No. 123, Jakarta",
    "dateOfBirth": "1990-01-15",
    "monthlyIncome": 5000000.0,
    "emergencyContacts": [
      {
        "id": 1,
        "contactName": "Budi Santoso",
        "contactPhone": "081234567890",
        "relationship": "ORANG_TUA",
        "createdAt": "2025-12-26T10:30:00"
      }
    ],
    "createdAt": "2025-12-26T10:30:00"
  }
}
```

**Error Response (404 Not Found):**

```json
{
  "success": false,
  "message": "Customer data not found",
  "data": null
}
```

**Error Response (401 Unauthorized):**

```json
{
  "success": false,
  "message": "Unauthorized",
  "data": null
}
```

---

### 3. Check if User Has Profile

Endpoint untuk mengecek apakah user sudah memiliki data customer.

**Endpoint:** `GET /api/customers/has-profile`

**Success Response (200 OK) - Profile Exists:**

```json
{
  "success": true,
  "message": "Customer profile exists",
  "data": true
}
```

**Success Response (200 OK) - Profile Not Found:**

```json
{
  "success": true,
  "message": "Customer profile not found",
  "data": false
}
```

**Error Response (401 Unauthorized):**

```json
{
  "success": false,
  "message": "Unauthorized",
  "data": null
}
```

---

## Business Rules

### NIK Validation

1. NIK harus unique di seluruh sistem
2. NIK harus 16 digit angka
3. Setiap user hanya bisa memiliki satu data customer

### Emergency Contacts

1. Customer bisa memiliki multiple kontak darurat
2. Hubungan yang valid: ORANG_TUA, SAUDARA, PASANGAN, TEMAN
3. Saat update profile, kontak darurat lama akan dihapus dan diganti dengan yang baru

### Data Requirements untuk Pinjaman

Sebelum customer dapat mengajukan pinjaman, mereka harus melengkapi:

1. NIK (wajib)
2. Address (opsional tapi direkomendasikan)
3. Date of Birth (opsional tapi direkomendasikan)
4. Monthly Income (wajib untuk perhitungan plafond)
5. Minimal 1 Emergency Contact (direkomendasikan)

---

## Usage Flow

### 1. Customer Login

```
POST /auth/login
{
  "username": "john_doe",
  "password": "password123"
}
```

### 2. Check if Profile Exists

```
GET /api/customers/has-profile
```

### 3. Create/Update Profile (if not exists or need update)

```
POST /api/customers/profile
{
  "nik": "3201234567890123",
  "address": "Jl. Contoh No. 123",
  "dateOfBirth": "1990-01-15",
  "monthlyIncome": 5000000.00,
  "emergencyContacts": [...]
}
```

### 4. Get Profile Data

```
GET /api/customers/profile
```

### 5. Apply for Loan (after profile complete)

```
POST /loans/apply
```

---

## Testing with Postman

### Setup

1. Import collection: `Genggamin_RBAC.postman_collection.json`
2. Login to get JWT token
3. Set token in Authorization header for all customer endpoints

### Test Scenarios

#### Scenario 1: First Time Profile Creation

1. Login as new customer
2. Check profile status (should return false)
3. Create profile with all required data
4. Verify profile creation successful
5. Get profile data to confirm

#### Scenario 2: Update Existing Profile

1. Login as existing customer
2. Get current profile data
3. Update profile with new information
4. Verify update successful
5. Check emergency contacts updated

#### Scenario 3: NIK Uniqueness Validation

1. Login as customer A
2. Create profile with NIK "1234567890123456"
3. Login as customer B
4. Try to create profile with same NIK
5. Should receive error: "NIK already registered by another customer"

#### Scenario 4: Complete Flow Before Loan Application

1. Register new user
2. Login
3. Check profile (should be false/not found)
4. Create complete customer profile
5. Check plafond eligibility (requires monthlyIncome)
6. Apply for loan

---

## Database Schema

### customers table

```sql
CREATE TABLE customers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    nik VARCHAR(20) NOT NULL UNIQUE,
    address VARCHAR(255),
    date_of_birth DATE,
    monthly_income DECIMAL(18,2),
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT fk_customer_user FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### emergency_contacts table

```sql
CREATE TABLE emergency_contacts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    contact_name VARCHAR(150) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    relationship VARCHAR(50),
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT fk_emergency_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);
```

---

## Error Codes

| Code | Message               | Description                    |
| ---- | --------------------- | ------------------------------ |
| 200  | Success               | Request berhasil               |
| 400  | Bad Request           | Data input tidak valid         |
| 401  | Unauthorized          | Token tidak valid atau expired |
| 404  | Not Found             | Data customer tidak ditemukan  |
| 500  | Internal Server Error | Kesalahan server               |

---

## Notes for Frontend Development

### Profile Completion Status

Frontend dapat menggunakan endpoint `GET /api/customers/has-profile` untuk:

- Menampilkan notifikasi "Complete your profile" di dashboard
- Redirect user ke halaman profile completion jika belum lengkap
- Disable loan application button jika profile belum lengkap

### Form Validation

- NIK: 16 digit numeric
- Phone: Format Indonesia (08xxx atau +62xxx)
- Date of Birth: Tidak boleh di masa depan
- Monthly Income: Positive number
- Relationship: Dropdown dengan options: ORANG_TUA, SAUDARA, PASANGAN, TEMAN

### UX Recommendations

1. Show progress indicator untuk profile completion
2. Allow partial save (draft mode) sebelum final submit
3. Validate NIK format di frontend sebelum submit
4. Show confirmation before updating existing profile
5. Allow edit individual emergency contacts without replacing all

---

## Security Considerations

1. **Authentication Required**: Semua endpoint memerlukan valid JWT token
2. **User Isolation**: User hanya bisa akses/update profile mereka sendiri
3. **NIK Privacy**: NIK adalah data sensitif, handle dengan hati-hati
4. **Data Validation**: Validate semua input di backend meskipun sudah divalidasi di frontend
5. **Audit Trail**: Log semua perubahan data customer untuk audit purposes

---

## Future Enhancements

1. **Document Upload**: Add KTP/ID card image upload
2. **Address Verification**: Integrate dengan API verifikasi alamat
3. **Income Verification**: Add salary slip or bank statement upload
4. **Credit Scoring**: Integrate dengan sistem credit scoring
5. **Profile Completion Score**: Calculate dan tampilkan completion percentage
6. **Profile History**: Track perubahan profile untuk audit
7. **Emergency Contact Verification**: Send SMS/Email verification ke emergency contacts
