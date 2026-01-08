# Dokumentasi Endpoint API - Genggamin Application

Dokumentasi lengkap semua endpoint yang tersedia dalam aplikasi Genggamin, diurutkan berdasarkan urutan pembuatan dari Auth hingga yang terbaru.

---

## 1. Auth Controller (`/auth`)

Controller untuk menangani autentikasi dan otorisasi user.

**File Terkait:**

- **Controller**: `controller/AuthController.java`
- **Service**: `service/UserService.java`, `service/TokenBlacklistService.java`, `service/PasswordResetService.java`, `service/EmailService.java`
- **Repository**: `repository/UserRepository.java`, `repository/PasswordResetTokenRepository.java`
- **Entity**: `entity/User.java`, `entity/Role.java`, `entity/PasswordResetToken.java`
- **DTO**: `dto/LoginRequest.java`, `dto/LoginResponse.java`, `dto/CreateUserRequest.java`, `dto/ForgotPasswordRequest.java`, `dto/ResetPasswordRequest.java`
- **Security**: `security/JwtUtil.java`
- **Config**: `config/SecurityConfig.java`, `config/RedisConfig.java`

### 1.1 POST `/auth/login`

**Deskripsi**: Login user dan mendapatkan JWT token  
**Akses**: Public (tanpa autentikasi)  
**Request Body**:

```json
{
  "username": "string",
  "password": "string"
}
```

**Response**: Token JWT dan informasi user  
**Fungsi**: Memvalidasi kredensial user, menggenerate JWT token yang berisi username dan roles

---

### 1.2 POST `/auth/register`

**Deskripsi**: Mendaftarkan user baru  
**Akses**: Public (tanpa autentikasi)  
**Request Body**:

```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "fullName": "string"
}
```

**Response**: ID user yang baru dibuat  
**Fungsi**: Membuat user baru dengan role default CUSTOMER. Field fullName akan digunakan untuk data customer. Field `roles` bersifat optional - jika tidak diisi, otomatis akan menjadi CUSTOMER

---

### 1.3 POST `/auth/logout`

**Deskripsi**: Logout user dan blacklist JWT token  
**Akses**: Authenticated (memerlukan token)  
**Header**: `Authorization: Bearer <token>`  
**Response**: Konfirmasi logout berhasil  
**Fungsi**: Menambahkan token ke blacklist agar tidak bisa digunakan lagi, menggunakan Redis untuk menyimpan blacklist

---

### 1.4 POST `/auth/forgot-password`

**Deskripsi**: Request reset password via email  
**Akses**: Public  
**Request Body**:

```json
{
  "email": "string"
}
```

**Response**: Konfirmasi link reset dikirim ke email  
**Fungsi**: Generate token reset password dan kirim link ke email user, token disimpan di Redis dengan expiry time

---

### 1.5 POST `/auth/reset-password`

**Deskripsi**: Reset password menggunakan token dari email  
**Akses**: Public  
**Request Body**:

```json
{
  "token": "string",
  "newPassword": "string"
}
```

**Response**: Konfirmasi password berhasil direset  
**Fungsi**: Validasi token dari email, update password baru, dan hapus token dari Redis

---

## 2. User Controller (`/users`)

Controller untuk mengelola data user.

**File Terkait:**

- **Controller**: `controller/UserController.java`
- **Service**: `service/UserService.java`
- **Repository**: `repository/UserRepository.java`, `repository/RoleRepository.java`
- **Entity**: `entity/User.java`, `entity/Role.java`
- **DTO**: `dto/UserResponse.java`, `dto/CreateUserRequest.java`, `dto/ApiResponse.java`

### 2.1 GET `/users`

**Deskripsi**: Mendapatkan semua user  
**Akses**: Authenticated  
**Response**: List semua user (dalam format DTO, tanpa password)  
**Fungsi**: Retrieve semua user dari database dan convert ke UserResponse DTO

---

### 2.2 POST `/users`

**Deskripsi**: Membuat user baru (alternative dari register)  
**Akses**: Authenticated  
**Request Body**:

```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "roles": ["ROLE_NAME"]
}
```

**Response**: Data user yang dibuat  
**Fungsi**: Create user baru dengan validasi

---

### 2.3 GET `/users/{userId}`

**Deskripsi**: Mendapatkan data user berdasarkan ID  
**Akses**: User yang sedang login (hanya bisa akses data sendiri)  
**Response**: Data user dalam format DTO  
**Fungsi**: Validasi bahwa user yang request adalah user yang sama dengan ID yang diminta, mencegah user lain mengakses data pribadi

---

## 3. Role Controller (`/roles`)

Controller untuk mengelola role/permissions.

**File Terkait:**

- **Controller**: `controller/RoleController.java`
- **Service**: `service/RoleService.java`
- **Repository**: `repository/RoleRepository.java`
- **Entity**: `entity/Role.java`
- **Config**: `config/DataInitializer.java` (untuk inisialisasi default roles)

### 3.1 GET `/roles`

**Deskripsi**: Mendapatkan semua role yang tersedia  
**Akses**: Authenticated  
**Response**: List semua role  
**Fungsi**: Retrieve semua role dari database untuk keperluan assignment

---

### 3.2 POST `/roles`

**Deskripsi**: Membuat role baru  
**Akses**: Authenticated  
**Request Body**:

```json
{
  "name": "ROLE_NAME",
  "description": "string"
}
```

**Response**: Data role yang dibuat  
**Fungsi**: Create role baru untuk sistem RBAC

---

## 4. Customer Controller (`/customers`)

Controller untuk mengelola data profil customer/nasabah.

**File Terkait:**

- **Controller**: `controller/CustomerController.java`
- **Service**: `service/CustomerService.java`, `service/UserService.java`
- **Repository**: `repository/CustomerRepository.java`, `repository/EmergencyContactRepository.java`, `repository/UserRepository.java`
- **Entity**: `entity/Customer.java`, `entity/EmergencyContact.java`, `entity/User.java`
- **DTO**: `dto/CustomerRequest.java`, `dto/CustomerResponse.java`, `dto/EmergencyContactRequest.java`, `dto/EmergencyContactResponse.java`, `dto/ApiResponse.java`

### 4.1 POST `/customers/profile`

**Deskripsi**: Membuat atau update profil customer  
**Akses**: Authenticated (user yang sedang login)  
**Request Body**:

```json
{
  "nik": "string",
  "dateOfBirth": "yyyy-MM-dd",
  "placeOfBirth": "string",
  "address": "string",
  "phone": "string",
  "monthlyIncome": "decimal",
  "occupation": "string",
  "emergencyContact": {
    "name": "string",
    "relationship": "string",
    "phone": "string"
  }
}
```

**Response**: Data customer yang tersimpan  
**Fungsi**: Jika user belum punya data customer, akan create baru. Jika sudah ada, akan update data yang ada. Field email dan fullName diambil dari tabel users (saat register)

---

### 4.2 GET `/customers/profile`

**Deskripsi**: Mendapatkan profil customer user yang sedang login  
**Akses**: Authenticated  
**Response**: Data customer lengkap termasuk emergency contact  
**Fungsi**: Retrieve data customer berdasarkan user ID yang sedang login

---

### 4.3 GET `/customers/has-profile`

**Deskripsi**: Cek apakah user sudah memiliki data customer  
**Akses**: Authenticated  
**Response**: Boolean (true/false)  
**Fungsi**: Quick check untuk menentukan apakah user perlu mengisi data customer atau tidak

---

### 4.4 PUT `/customers/profile`

**Deskripsi**: Update profil customer (khusus untuk update)  
**Akses**: Authenticated  
**Request Body**: Sama seperti POST `/customers/profile`  
**Response**: Data customer yang terupdate  
**Fungsi**: Khusus untuk update data customer yang sudah ada (alternative dari POST yang bisa create atau update)

---

## 5. Loan Controller (`/loans`)

Controller untuk mengelola pengajuan dan proses approval pinjaman dengan workflow RBAC.

**File Terkait:**

- **Controller**: `controller/LoanController.java`
- **Service**: `service/LoanService.java`, `service/CustomerService.java`, `service/PlafondService.java`
- **Repository**: `repository/LoanRepository.java`, `repository/CustomerRepository.java`, `repository/PlafondRepository.java`, `repository/UserRepository.java`
- **Entity**: `entity/Loan.java`, `entity/Customer.java`, `entity/Plafond.java`, `entity/User.java`
- **DTO**: `dto/LoanRequest.java`, `dto/LoanResponse.java`, `dto/LoanActionRequest.java`, `dto/ApiResponse.java`
- **Enum**: `LoanStatus` (dalam entity Loan)

### 5.1 POST `/loans/submit`

**Deskripsi**: Customer submit pengajuan pinjaman  
**Akses**: Role CUSTOMER atau ADMIN  
**Request Body**:

```json
{
  "plafondId": "long",
  "amount": "decimal",
  "purpose": "string",
  "tenor": "integer (months)"
}
```

**Response**: Data loan yang disubmit dengan status SUBMITTED  
**Fungsi**: Customer mengajukan pinjaman, validasi kelengkapan data customer dan plafond yang dipilih

---

### 5.2 GET `/loans/my-loans`

**Deskripsi**: Customer melihat semua pengajuan pinjaman miliknya  
**Akses**: Role CUSTOMER atau ADMIN  
**Response**: List loan beserta status dan history  
**Fungsi**: Retrieve semua loan yang disubmit oleh customer yang sedang login

---

### 5.3 GET `/loans/review`

**Deskripsi**: Marketing melihat loan yang perlu direview  
**Akses**: Role MARKETING atau ADMIN  
**Response**: List loan dengan status SUBMITTED  
**Fungsi**: Menampilkan antrian loan yang perlu direview oleh marketing

---

### 5.4 POST `/loans/review/{loanId}`

**Deskripsi**: Marketing mereview loan (approve/reject)  
**Akses**: Role MARKETING atau ADMIN  
**Request Body**:

```json
{
  "action": "APPROVE/REJECT",
  "notes": "string"
}
```

**Response**: Data loan dengan status updated (REVIEWED atau REJECTED)  
**Fungsi**: Marketing review loan, jika approve akan lanjut ke BRANCH_MANAGER, jika reject akan langsung REJECTED

---

### 5.5 GET `/loans/approve`

**Deskripsi**: Branch Manager melihat loan yang perlu approval  
**Akses**: Role BRANCH_MANAGER atau ADMIN  
**Response**: List loan dengan status REVIEWED  
**Fungsi**: Menampilkan loan yang sudah direview marketing dan menunggu approval branch manager

---

### 5.6 POST `/loans/approve/{loanId}`

**Deskripsi**: Branch Manager approve/reject loan  
**Akses**: Role BRANCH_MANAGER atau ADMIN  
**Request Body**:

```json
{
  "action": "APPROVE/REJECT",
  "notes": "string"
}
```

**Response**: Data loan dengan status updated (APPROVED atau REJECTED)  
**Fungsi**: Branch manager melakukan final approval, jika approve akan lanjut ke tahap disbursement

---

### 5.7 GET `/loans/disburse`

**Deskripsi**: Back Office melihat loan yang siap dicairkan  
**Akses**: Role BACK_OFFICE atau ADMIN  
**Response**: List loan dengan status APPROVED  
**Fungsi**: Menampilkan loan yang sudah diapprove dan siap untuk proses pencairan

---

### 5.8 POST `/loans/disburse/{loanId}`

**Deskripsi**: Back Office mencairkan dana loan  
**Akses**: Role BACK_OFFICE atau ADMIN  
**Request Body**:

```json
{
  "action": "DISBURSE",
  "notes": "string"
}
```

**Response**: Data loan dengan status DISBURSED  
**Fungsi**: Back office melakukan pencairan dana, mengubah status loan menjadi DISBURSED

---

### 5.9 GET `/loans/all`

**Deskripsi**: Admin melihat semua loan  
**Akses**: Role ADMIN only  
**Response**: List semua loan dari semua customer  
**Fungsi**: Admin dashboard untuk monitoring semua loan

---

### 5.10 GET `/loans/{loanId}`

**Deskripsi**: Melihat detail loan berdasarkan ID  
**Akses**: Authenticated users  
**Response**: Detail loan lengkap  
**Fungsi**: Retrieve detail loan tertentu

---

## 6. Plafond Controller (`/plafonds`)

Controller untuk mengelola master data plafond pinjaman (limit kredit berdasarkan income).

**File Terkait:**

- **Controller**: `controller/PlafondController.java`
- **Service**: `service/PlafondService.java`
- **Repository**: `repository/PlafondRepository.java`
- **Entity**: `entity/Plafond.java`
- **DTO**: `dto/PlafondRequest.java`, `dto/PlafondResponse.java`, `dto/ApiResponse.java`

### 6.1 GET `/plafonds`

**Deskripsi**: Mendapatkan semua plafond  
**Akses**: Public (tanpa autentikasi)  
**Response**: List semua plafond termasuk yang tidak aktif  
**Fungsi**: Retrieve semua plafond untuk keperluan informasi

---

### 6.2 GET `/plafonds/active`

**Deskripsi**: Mendapatkan hanya plafond yang aktif  
**Akses**: Public  
**Response**: List plafond dengan status isActive = true  
**Fungsi**: Menampilkan plafond yang tersedia untuk customer

---

### 6.3 GET `/plafonds/{id}`

**Deskripsi**: Mendapatkan detail plafond berdasarkan ID  
**Akses**: Public  
**Response**: Detail plafond tertentu  
**Fungsi**: Retrieve informasi detail plafond

---

### 6.4 GET `/plafonds/by-income/{income}`

**Deskripsi**: Mendapatkan plafond berdasarkan income  
**Akses**: Public (untuk cek eligibilitas)  
**Response**: List plafond yang sesuai dengan income range  
**Fungsi**: Customer bisa cek plafond apa saja yang eligible berdasarkan income mereka

---

### 6.5 POST `/plafonds`

**Deskripsi**: Membuat plafond baru  
**Akses**: Role ADMIN only  
**Request Body**:

```json
{
  "name": "string",
  "minIncome": "decimal",
  "maxIncome": "decimal",
  "maxLoanAmount": "decimal",
  "interestRate": "decimal",
  "maxTenor": "integer",
  "description": "string"
}
```

**Response**: Data plafond yang dibuat  
**Fungsi**: Admin membuat konfigurasi plafond baru dengan validasi income range tidak overlap

---

### 6.6 PUT `/plafonds/{id}`

**Deskripsi**: Update plafond existing  
**Akses**: Role ADMIN only  
**Request Body**: Sama seperti POST  
**Response**: Data plafond yang terupdate  
**Fungsi**: Admin update konfigurasi plafond

---

### 6.7 DELETE `/plafonds/{id}`

**Deskripsi**: Soft delete plafond  
**Akses**: Role ADMIN only  
**Response**: Konfirmasi delete berhasil  
**Fungsi**: Menandai plafond sebagai deleted (soft delete) dan mencatat siapa yang menghapus dan kapan. Data tidak benar-benar dihapus dari database

---

### 6.8 PATCH `/plafonds/{id}/toggle-status`

**Deskripsi**: Toggle status aktif/non-aktif plafond  
**Akses**: Role ADMIN only  
**Response**: Data plafond dengan status baru  
**Fungsi**: Mengubah isActive menjadi true/false, berguna untuk temporarily menonaktifkan plafond tanpa menghapusnya

---

### 6.9 PATCH `/plafonds/{id}/restore`

**Deskripsi**: Restore plafond yang di-soft delete  
**Akses**: Role ADMIN only  
**Response**: Data plafond yang direstore  
**Fungsi**: Mengembalikan plafond yang sudah di-soft delete, menghilangkan flag deleted dan deletedAt/deletedBy

---

## Workflow Proses Loan

```
1. CUSTOMER submit loan → Status: SUBMITTED
   ↓
2. MARKETING review → Status: REVIEWED (approve) / REJECTED (reject)
   ↓
3. BRANCH_MANAGER approve → Status: APPROVED (approve) / REJECTED (reject)
   ↓
4. BACK_OFFICE disburse → Status: DISBURSED
```

## Fitur Keamanan

1. **JWT Authentication**: Semua endpoint (kecuali public) memerlukan JWT token
2. **Role-Based Access Control (RBAC)**: Setiap endpoint memiliki pembatasan role tertentu
3. **Token Blacklist**: Logout menggunakan Redis untuk blacklist token
4. **Password Reset**: Menggunakan token dengan expiry time di Redis
5. **Soft Delete**: Data penting tidak dihapus permanen dari database
6. **User Data Privacy**: User hanya bisa akses data dirinya sendiri

## Roles dalam Sistem

1. **CUSTOMER**: Mengajukan pinjaman dan melihat status pinjaman mereka
2. **MARKETING**: Review pengajuan pinjaman customer
3. **BRANCH_MANAGER**: Approve/reject pinjaman yang sudah direview
4. **BACK_OFFICE**: Mencairkan dana pinjaman yang sudah diapprove
5. **ADMIN**: Akses penuh ke semua endpoint, manage master data

## Teknologi yang Digunakan

- **Spring Boot**: Framework utama
- **Spring Security**: Autentikasi dan otorisasi
- **JWT**: Token-based authentication
- **Redis**: Token blacklist dan password reset token
- **PostgreSQL**: Database utama
- **Lombok**: Reduce boilerplate code
- **MapStruct/Manual mapping**: Entity to DTO conversion
