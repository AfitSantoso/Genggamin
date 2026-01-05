# Dokumentasi Struktur Project Genggamin

Dokumen ini menjelaskan setiap file yang ada dalam folder `config`, `controller`, `dto`, `entity`, `repository`, `security`, dan `service` pada aplikasi Genggamin.

---

## 📁 Package: `config`

### 1. [**DataInitializer.java**](src/main/java/com/example/genggamin/config/DataInitializer.java)

**Fungsi**: Inisialisasi data awal saat aplikasi dijalankan pertama kali.

**Deskripsi Detail**:

- Mengimplementasikan `CommandLineRunner` yang dieksekusi setelah aplikasi Spring Boot selesai start
- Membuat role default (ADMIN, MANAGER, OFFICER, CUSTOMER) jika belum ada di database
- Membuat user default dengan role yang sesuai untuk keperluan testing dan akses awal
- Menggunakan BCrypt untuk enkripsi password user default
- Memastikan data referensi yang dibutuhkan sudah tersedia sebelum aplikasi digunakan

**Kapan Digunakan**: Otomatis dijalankan setiap kali aplikasi start.

---

### 2. [**RedisConfig.java**](src/main/java/com/example/genggamin/config/RedisConfig.java)

**Fungsi**: Konfigurasi Redis untuk caching data.

**Deskripsi Detail**:

- Mengaktifkan caching menggunakan annotation `@EnableCaching`
- Mengkonfigurasi `RedisTemplate` untuk operasi Redis manual
- Membuat `RedisCacheManager` dengan konfigurasi cache custom
- Mengatur serializer untuk key (String) dan value (JSON)
- Mengkonfigurasi Jackson ObjectMapper dengan JavaTimeModule untuk serialisasi LocalDateTime
- Mengatur TTL (Time To Live) untuk cache: users (5 menit), roles (10 menit), default (1 menit)
- Mendukung polymorphic type handling untuk serialisasi object kompleks

**Kapan Digunakan**: Secara otomatis saat aplikasi memerlukan caching data.

---

### 3. [**SecurityConfig.java**](src/main/java/com/example/genggamin/config/SecurityConfig.java)

**Fungsi**: Konfigurasi keamanan aplikasi menggunakan Spring Security.

**Deskripsi Detail**:

- Mengaktifkan method-level security dengan `@EnableMethodSecurity`
- Mendefinisikan `PasswordEncoder` menggunakan BCrypt untuk enkripsi password
- Mengkonfigurasi `SecurityFilterChain` dengan aturan:
  - Public endpoints: `/api/auth/**` (login, register, forgot password, dll)
  - Protected endpoints: semua endpoint lainnya memerlukan autentikasi
- Menonaktifkan CSRF karena menggunakan JWT (stateless)
- Mengatur session management menjadi STATELESS
- Menambahkan `JwtAuthenticationFilter` sebelum `UsernamePasswordAuthenticationFilter`
- Mengkonfigurasi custom exception handlers:
  - `RestAuthenticationEntryPoint` untuk unauthorized access (401)
  - `RestAccessDeniedHandler` untuk forbidden access (403)

**Kapan Digunakan**: Setiap request HTTP akan melewati filter ini.

---

## 📁 Package: `controller`

### 1. [**AuthController.java**](src/main/java/com/example/genggamin/controller/AuthController.java)

**Fungsi**: Menangani endpoint untuk autentikasi dan otorisasi.

**Endpoints**:

- `POST /api/auth/register` - Registrasi user baru
- `POST /api/auth/login` - Login dan mendapatkan JWT token
- `POST /api/auth/logout` - Logout dan blacklist token
- `POST /api/auth/forgot-password` - Request reset password
- `POST /api/auth/reset-password` - Reset password menggunakan token

**Deskripsi Detail**:

- Mengelola registrasi user dengan validasi email unik
- Proses login menghasilkan JWT token untuk akses ke endpoint protected
- Logout menambahkan token ke blacklist agar tidak bisa digunakan lagi
- Forgot password mengirim email dengan link reset password
- Reset password memvalidasi token dan mengubah password

---

### 2. [**CustomerController.java**](src/main/java/com/example/genggamin/controller/CustomerController.java)

**Fungsi**: CRUD operations untuk Customer Profile.

**Endpoints**:

- `POST /api/customers` - Membuat profile customer baru
- `GET /api/customers` - Mendapatkan semua customers (dengan pagination)
- `GET /api/customers/{id}` - Mendapatkan customer berdasarkan ID
- `PUT /api/customers/{id}` - Update customer profile
- `DELETE /api/customers/{id}` - Soft delete customer

**Deskripsi Detail**:

- Khusus untuk user dengan role CUSTOMER
- Mengelola data profile lengkap termasuk NIK, alamat, tanggal lahir, income
- Mengelola emergency contact untuk customer
- Mendukung pagination dan filter untuk list customers
- Implementasi soft delete (data tidak benar-benar dihapus)

**Access Control**:

- ADMIN, MANAGER: akses penuh
- OFFICER: read dan update
- CUSTOMER: hanya bisa akses profile sendiri

---

### 3. [**LoanController.java**](src/main/java/com/example/genggamin/controller/LoanController.java)

**Fungsi**: Mengelola pengajuan dan approval pinjaman.

**Endpoints**:

- `POST /api/loans` - Mengajukan pinjaman baru
- `GET /api/loans` - List semua pinjaman
- `GET /api/loans/{id}` - Detail pinjaman
- `PUT /api/loans/{id}/approve` - Approve pinjaman
- `PUT /api/loans/{id}/reject` - Reject pinjaman
- `PUT /api/loans/{id}/disburse` - Pencairan dana
- `DELETE /api/loans/{id}` - Delete pengajuan pinjaman

**Deskripsi Detail**:

- Customer mengajukan pinjaman dengan jumlah dan tenor tertentu
- System otomatis cek plafond yang tersedia
- Officer/Manager melakukan approval atau rejection
- Setelah approve, loan bisa dicairkan (disbursed)
- Tracking status loan: PENDING, APPROVED, REJECTED, DISBURSED

**Access Control**:

- CUSTOMER: bisa create dan view pinjaman sendiri
- OFFICER: bisa approve/reject pinjaman
- MANAGER: akses penuh termasuk disburse
- ADMIN: akses penuh

---

### 4. [**PlafondController.java**](src/main/java/com/example/genggamin/controller/PlafondController.java)

**Fungsi**: Mengelola plafond (limit kredit) customer.

**Endpoints**:

- `POST /api/plafonds` - Buat plafond baru
- `GET /api/plafonds` - List semua plafonds
- `GET /api/plafonds/{id}` - Detail plafond
- `GET /api/plafonds/customer/{customerId}` - Plafond by customer
- `PUT /api/plafonds/{id}` - Update plafond
- `DELETE /api/plafonds/{id}` - Soft delete plafond

**Deskripsi Detail**:

- Menentukan total limit yang tersedia untuk customer
- Tracking used amount (sudah dipakai berapa)
- Tracking available amount (sisa yang bisa dipakai)
- Support soft delete untuk audit trail
- Validasi agar used amount tidak melebihi total limit

**Access Control**:

- MANAGER, ADMIN: akses penuh
- OFFICER: read only
- CUSTOMER: hanya bisa view plafond sendiri

---

### 5. [**RoleController.java**](src/main/java/com/example/genggamin/controller/RoleController.java)

**Fungsi**: Mengelola role dan permission dalam sistem.

**Endpoints**:

- `GET /api/roles` - List semua roles
- `GET /api/roles/{id}` - Detail role
- `POST /api/roles` - Buat role baru (reserved untuk future use)

**Deskripsi Detail**:

- Menampilkan daftar role yang ada: ADMIN, MANAGER, OFFICER, CUSTOMER
- Read-only untuk role management (role dibuat otomatis via DataInitializer)
- Setiap role memiliki authority/permission tertentu

**Access Control**:

- ADMIN, MANAGER: bisa view roles
- Other roles: terbatas akses

---

### 6. [**UserController.java**](src/main/java/com/example/genggamin/controller/UserController.java)

**Fungsi**: Mengelola user accounts.

**Endpoints**:

- `POST /api/users` - Buat user baru (oleh admin)
- `GET /api/users` - List semua users
- `GET /api/users/{id}` - Detail user
- `PUT /api/users/{id}` - Update user
- `PUT /api/users/{id}/roles` - Update user roles
- `DELETE /api/users/{id}` - Delete user

**Deskripsi Detail**:

- CRUD lengkap untuk user management
- Admin bisa membuat user dengan role tertentu
- Support assign/revoke roles
- Validasi email dan username harus unik
- Password di-encrypt menggunakan BCrypt

**Access Control**:

- ADMIN: akses penuh
- MANAGER: read dan update terbatas
- Other roles: tidak bisa akses endpoint ini

---

## 📁 Package: `dto` (Data Transfer Objects)

### Request DTOs

#### 1. [**CreateUserRequest.java**](src/main/java/com/example/genggamin/dto/CreateUserRequest.java)

**Fungsi**: DTO untuk membuat user baru.

**Fields**:

- `username`: Username untuk login
- `email`: Email user (harus unique)
- `password`: Password (akan di-encrypt)
- `roles`: Set of role IDs yang akan diberikan ke user

**Validasi**: NotBlank untuk semua field, Email format untuk email.

---

#### 2. [**CustomerRequest.java**](src/main/java/com/example/genggamin/dto/CustomerRequest.java)

**Fungsi**: DTO untuk membuat/update customer profile.

**Fields**:

- `userId`: ID user yang terkait
- `nik`: Nomor Induk Kependudukan (unique)
- `address`: Alamat lengkap
- `dateOfBirth`: Tanggal lahir
- `monthlyIncome`: Penghasilan bulanan
- `employmentStatus`: Status pekerjaan (EMPLOYED, SELF_EMPLOYED, UNEMPLOYED)
- `phoneNumber`: Nomor telepon
- `emergencyContacts`: List emergency contacts

---

#### 3. [**EmergencyContactRequest.java**](src/main/java/com/example/genggamin/dto/EmergencyContactRequest.java)

**Fungsi**: DTO untuk emergency contact customer.

**Fields**:

- `name`: Nama kontak darurat
- `relationship`: Hubungan dengan customer
- `phoneNumber`: Nomor telepon kontak
- `address`: Alamat kontak

---

#### 4. [**ForgotPasswordRequest.java**](src/main/java/com/example/genggamin/dto/ForgotPasswordRequest.java)

**Fungsi**: DTO untuk request reset password.

**Fields**:

- `email`: Email user yang lupa password

---

#### 5. [**LoanActionRequest.java**](src/main/java/com/example/genggamin/dto/LoanActionRequest.java)

**Fungsi**: DTO untuk approve/reject loan.

**Fields**:

- `actionBy`: User ID yang melakukan action
- `notes`: Catatan approval/rejection

---

#### 6. [**LoanRequest.java**](src/main/java/com/example/genggamin/dto/LoanRequest.java)

**Fungsi**: DTO untuk pengajuan pinjaman.

**Fields**:

- `customerId`: ID customer yang mengajukan
- `amount`: Jumlah pinjaman
- `tenorMonths`: Tenor dalam bulan
- `purpose`: Tujuan pinjaman
- `notes`: Catatan tambahan

---

#### 7. [**LoginRequest.java**](src/main/java/com/example/genggamin/dto/LoginRequest.java)

**Fungsi**: DTO untuk login.

**Fields**:

- `username`: Username atau email
- `password`: Password

---

#### 8. [**PlafondRequest.java**](src/main/java/com/example/genggamin/dto/PlafondRequest.java)

**Fungsi**: DTO untuk create/update plafond.

**Fields**:

- `customerId`: ID customer
- `totalLimit`: Total limit yang diberikan
- `usedAmount`: Jumlah yang sudah digunakan
- `notes`: Catatan plafond

---

#### 9. [**ResetPasswordRequest.java**](src/main/java/com/example/genggamin/dto/ResetPasswordRequest.java)

**Fungsi**: DTO untuk reset password dengan token.

**Fields**:

- `token`: Token dari email
- `newPassword`: Password baru
- `confirmPassword`: Konfirmasi password baru

---

### Response DTOs

#### 1. [**ApiResponse.java**](src/main/java/com/example/genggamin/dto/ApiResponse.java)

**Fungsi**: Generic response wrapper untuk API.

**Fields**:

- `success`: Boolean status sukses/gagal
- `message`: Pesan response
- `data`: Data payload (generic type)

**Kegunaan**: Standardisasi format response API agar konsisten.

---

#### 2. [**CustomerResponse.java**](src/main/java/com/example/genggamin/dto/CustomerResponse.java)

**Fungsi**: DTO response untuk customer data.

**Fields**: Berisi semua informasi customer termasuk user info dan emergency contacts.

---

#### 3. [**EmergencyContactResponse.java**](src/main/java/com/example/genggamin/dto/EmergencyContactResponse.java)

**Fungsi**: DTO response untuk emergency contact.

---

#### 4. [**LoanResponse.java**](src/main/java/com/example/genggamin/dto/LoanResponse.java)

**Fungsi**: DTO response untuk loan data.

**Fields**: Berisi detail pinjaman, customer info, status, approval info, dll.

---

#### 5. [**LoginResponse.java**](src/main/java/com/example/genggamin/dto/LoginResponse.java)

**Fungsi**: DTO response setelah login sukses.

**Fields**:

- `token`: JWT access token
- `type`: Token type (Bearer)
- `username`: Username yang login
- `email`: Email user
- `roles`: List role yang dimiliki

---

#### 6. [**PlafondResponse.java**](src/main/java/com/example/genggamin/dto/PlafondResponse.java)

**Fungsi**: DTO response untuk plafond data.

**Fields**: Total limit, used amount, available amount, customer info, dll.

---

#### 7. [**RoleResponse.java**](src/main/java/com/example/genggamin/dto/RoleResponse.java)

**Fungsi**: DTO response untuk role data.

**Fields**:

- `id`: Role ID
- `name`: Role name

---

#### 8. [**UserResponse.java**](src/main/java/com/example/genggamin/dto/UserResponse.java)

**Fungsi**: DTO response untuk user data.

**Fields**:

- `id`: User ID
- `username`: Username
- `email`: Email
- `roles`: List roles
- `createdAt`: Timestamp pembuatan

---

## 📁 Package: `entity`

### 1. [**User.java**](src/main/java/com/example/genggamin/entity/User.java)

**Fungsi**: Entity untuk user account.

**Attributes**:

- `id`: Primary key (auto-increment)
- `username`: Username unique
- `email`: Email unique
- `password`: Password encrypted dengan BCrypt
- `roles`: Many-to-Many relationship dengan Role
- `enabled`: Status aktif/non-aktif user
- `createdAt`: Timestamp pembuatan
- `updatedAt`: Timestamp update terakhir

**Relationships**:

- Many-to-Many dengan `Role`
- One-to-One dengan `Customer`

---

### 2. [**Role.java**](src/main/java/com/example/genggamin/entity/Role.java)

**Fungsi**: Entity untuk role/authority.

**Attributes**:

- `id`: Primary key
- `name`: Nama role (ADMIN, MANAGER, OFFICER, CUSTOMER)
- `description`: Deskripsi role

**Relationships**:

- Many-to-Many dengan `User`

**Roles Available**:

- **ADMIN**: Full access ke semua fitur
- **MANAGER**: Manage loans, plafonds, users
- **OFFICER**: Process loans, view customers
- **CUSTOMER**: Basic access, manage profile sendiri

---

### 3. [**Customer.java**](src/main/java/com/example/genggamin/entity/Customer.java)

**Fungsi**: Entity untuk customer profile.

**Attributes**:

- `id`: Primary key
- `userId`: Foreign key ke User (unique)
- `nik`: Nomor Induk Kependudukan (unique)
- `address`: Alamat
- `dateOfBirth`: Tanggal lahir
- `monthlyIncome`: Penghasilan bulanan (BigDecimal)
- `employmentStatus`: Enum status pekerjaan
- `phoneNumber`: Nomor telepon
- `isDeleted`: Flag soft delete
- `createdAt`, `updatedAt`, `deletedAt`: Timestamps

**Relationships**:

- One-to-One dengan `User`
- One-to-Many dengan `EmergencyContact`
- One-to-Many dengan `Loan`
- One-to-One dengan `Plafond`

---

### 4. [**EmergencyContact.java**](src/main/java/com/example/genggamin/entity/EmergencyContact.java)

**Fungsi**: Entity untuk kontak darurat customer.

**Attributes**:

- `id`: Primary key
- `customerId`: Foreign key ke Customer
- `name`: Nama kontak
- `relationship`: Hubungan dengan customer
- `phoneNumber`: Nomor telepon
- `address`: Alamat kontak

**Relationships**:

- Many-to-One dengan `Customer`

---

### 5. [**Plafond.java**](src/main/java/com/example/genggamin/entity/Plafond.java)

**Fungsi**: Entity untuk plafond (limit kredit) customer.

**Attributes**:

- `id`: Primary key
- `customerId`: Foreign key ke Customer (unique)
- `totalLimit`: Total limit yang diberikan (BigDecimal)
- `usedAmount`: Jumlah yang sudah dipakai (BigDecimal)
- `availableAmount`: Sisa limit yang tersedia (BigDecimal, calculated)
- `isDeleted`: Flag soft delete
- `notes`: Catatan plafond
- `createdAt`, `updatedAt`, `deletedAt`: Timestamps

**Relationships**:

- One-to-One dengan `Customer`

**Business Logic**:

- `availableAmount = totalLimit - usedAmount`
- Used amount akan bertambah saat loan approved dan disbursed

---

### 6. [**Loan.java**](src/main/java/com/example/genggamin/entity/Loan.java)

**Fungsi**: Entity untuk pengajuan pinjaman.

**Attributes**:

- `id`: Primary key
- `customerId`: Foreign key ke Customer
- `plafondId`: Foreign key ke Plafond
- `amount`: Jumlah pinjaman (BigDecimal)
- `tenorMonths`: Jangka waktu dalam bulan
- `purpose`: Tujuan pinjaman
- `status`: Enum status loan (PENDING, APPROVED, REJECTED, DISBURSED, CANCELLED)
- `appliedAt`: Tanggal pengajuan
- `approvedAt`, `approvedBy`: Approval info
- `disbursedAt`, `disbursedBy`: Disbursement info
- `rejectedAt`, `rejectedBy`, `rejectionReason`: Rejection info
- `notes`: Catatan

**Relationships**:

- Many-to-One dengan `Customer`
- Many-to-One dengan `Plafond`

**Workflow**:

1. Customer create loan → status PENDING
2. Officer/Manager approve → status APPROVED
3. Manager disburse → status DISBURSED
4. Atau reject → status REJECTED

---

### 7. [**PasswordResetToken.java**](src/main/java/com/example/genggamin/entity/PasswordResetToken.java)

**Fungsi**: Entity untuk menyimpan token reset password.

**Attributes**:

- `id`: Primary key
- `token`: Token unique untuk reset password
- `userId`: Foreign key ke User
- `expiryDate`: Tanggal kadaluarsa token
- `used`: Flag apakah token sudah digunakan

**Relationships**:

- Many-to-One dengan `User`

**Workflow**:

1. User request forgot password
2. System generate token dengan expiry time (1 jam)
3. Token dikirim via email
4. User klik link dan reset password
5. Token ditandai sebagai used

---

## 📁 Package: `repository`

### 1. [**UserRepository.java**](src/main/java/com/example/genggamin/repository/UserRepository.java)

**Fungsi**: Repository untuk operasi database User.

**Methods**:

- `findByUsername()`: Cari user by username
- `findByEmail()`: Cari user by email
- `findByUsernameOrEmail()`: Cari by username atau email
- `existsByUsername()`: Cek apakah username sudah ada
- `existsByEmail()`: Cek apakah email sudah ada

**Extends**: `JpaRepository<User, Long>`

---

### 2. [**RoleRepository.java**](src/main/java/com/example/genggamin/repository/RoleRepository.java)

**Fungsi**: Repository untuk operasi database Role.

**Methods**:

- `findByName()`: Cari role by name

**Extends**: `JpaRepository<Role, Long>`

---

### 3. [**CustomerRepository.java**](src/main/java/com/example/genggamin/repository/CustomerRepository.java)

**Fungsi**: Repository untuk operasi database Customer.

**Methods**:

- `findByUserId()`: Cari customer by user ID
- `findByNik()`: Cari customer by NIK
- `findByIsDeletedFalse()`: Ambil semua customer yang tidak dihapus
- `existsByNik()`: Cek apakah NIK sudah ada

**Extends**: `JpaRepository<Customer, Long>`

**Note**: Support soft delete dengan flag `isDeleted`

---

### 4. [**EmergencyContactRepository.java**](src/main/java/com/example/genggamin/repository/EmergencyContactRepository.java)

**Fungsi**: Repository untuk operasi database EmergencyContact.

**Methods**:

- `findByCustomerId()`: Ambil semua emergency contact by customer

**Extends**: `JpaRepository<EmergencyContact, Long>`

---

### 5. [**PlafondRepository.java**](src/main/java/com/example/genggamin/repository/PlafondRepository.java)

**Fungsi**: Repository untuk operasi database Plafond.

**Methods**:

- `findByCustomerId()`: Cari plafond by customer ID
- `findByIsDeletedFalse()`: Ambil semua plafond yang tidak dihapus

**Extends**: `JpaRepository<Plafond, Long>`

---

### 6. [**LoanRepository.java**](src/main/java/com/example/genggamin/repository/LoanRepository.java)

**Fungsi**: Repository untuk operasi database Loan.

**Methods**:

- `findByCustomerId()`: Ambil semua loan by customer
- `findByStatus()`: Ambil loan by status
- `findByCustomerIdAndStatus()`: Filter by customer dan status

**Extends**: `JpaRepository<Loan, Long>`

---

### 7. [**PasswordResetTokenRepository.java**](src/main/java/com/example/genggamin/repository/PasswordResetTokenRepository.java)

**Fungsi**: Repository untuk operasi database PasswordResetToken.

**Methods**:

- `findByToken()`: Cari token by token string
- `deleteByUserId()`: Hapus semua token by user (cleanup)

**Extends**: `JpaRepository<PasswordResetToken, Long>`

---

## 📁 Package: `security`

### 1. [**JwtUtil.java**](src/main/java/com/example/genggamin/security/JwtUtil.java)

**Fungsi**: Utility class untuk generate dan validate JWT token.

**Methods**:

- `generateToken(UserDetails)`: Generate JWT token dengan username dan roles
- `extractUsername(String token)`: Extract username dari token
- `extractExpiration(String token)`: Extract expiration time dari token
- `validateToken(String token, UserDetails)`: Validasi apakah token valid dan belum expired
- `isTokenExpired(String token)`: Cek apakah token sudah expired

**Configuration**:

- Secret key: dari `application.properties` (`jwt.secret`)
- Expiration time: dari `application.properties` (`jwt.expiration`)
- Algorithm: HMAC-SHA256

---

### 2. [**JwtAuthenticationFilter.java**](src/main/java/com/example/genggamin/security/JwtAuthenticationFilter.java)

**Fungsi**: Filter untuk intercept setiap request dan validasi JWT token.

**Flow**:

1. Extract token dari header `Authorization: Bearer <token>`
2. Validasi token menggunakan `JwtUtil`
3. Cek apakah token ada di blacklist (logout)
4. Jika valid, extract username dan load user details
5. Set authentication ke SecurityContext
6. Forward request ke filter berikutnya

**Order**: Dijalankan sebelum `UsernamePasswordAuthenticationFilter`

---

### 3. [**RestAuthenticationEntryPoint.java**](src/main/java/com/example/genggamin/security/RestAuthenticationEntryPoint.java)

**Fungsi**: Handler untuk unauthorized access (401).

**Trigger**: Ketika user mencoba akses protected endpoint tanpa token atau token invalid.

**Response**:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

---

### 4. [**RestAccessDeniedHandler.java**](src/main/java/com/example/genggamin/security/RestAccessDeniedHandler.java)

**Fungsi**: Handler untuk forbidden access (403).

**Trigger**: Ketika user sudah authenticated tapi tidak punya permission untuk akses resource.

**Response**:

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to access this resource"
}
```

**Contoh**: Customer mencoba akses endpoint yang hanya untuk ADMIN.

---

## 📁 Package: `service`

### 1. [**UserService.java**](src/main/java/com/example/genggamin/service/UserService.java)

**Fungsi**: Business logic untuk user management.

**Methods**:

- `createUser()`: Membuat user baru dengan validasi
- `getUserById()`: Get user by ID
- `getAllUsers()`: List semua users
- `updateUser()`: Update user data
- `deleteUser()`: Hapus user
- `assignRolesToUser()`: Assign roles ke user
- `loadUserByUsername()`: Untuk authentication (implements UserDetailsService)

**Features**:

- Password encryption menggunakan BCrypt
- Validasi email dan username unique
- Caching user data di Redis
- Integration dengan Spring Security

---

### 2. [**RoleService.java**](src/main/java/com/example/genggamin/service/RoleService.java)

**Fungsi**: Business logic untuk role management.

**Methods**:

- `getAllRoles()`: List semua roles
- `getRoleById()`: Get role by ID
- `getRoleByName()`: Get role by name
- `createRole()`: Create role baru (reserved)

**Features**:

- Caching role data di Redis
- Read-only untuk existing roles

---

### 3. [**CustomerService.java**](src/main/java/com/example/genggamin/service/CustomerService.java)

**Fungsi**: Business logic untuk customer profile management.

**Methods**:

- `createCustomer()`: Membuat customer profile
- `getCustomerById()`: Get customer by ID
- `getAllCustomers()`: List customers dengan pagination
- `updateCustomer()`: Update customer profile
- `deleteCustomer()`: Soft delete customer
- `getCustomerByUserId()`: Get customer by user ID

**Features**:

- Validasi NIK unique
- Management emergency contacts
- Soft delete dengan audit trail
- Business rules validation

---

### 4. [**PlafondService.java**](src/main/java/com/example/genggamin/service/PlafondService.java)

**Fungsi**: Business logic untuk plafond management.

**Methods**:

- `createPlafond()`: Buat plafond untuk customer
- `getPlafondById()`: Get plafond by ID
- `getPlafondByCustomerId()`: Get plafond by customer
- `updatePlafond()`: Update plafond
- `updateUsedAmount()`: Update used amount (called by loan service)
- `deletePlafond()`: Soft delete plafond
- `calculateAvailableAmount()`: Hitung available amount

**Business Rules**:

- One customer = one plafond
- Used amount tidak boleh > total limit
- Available amount auto-calculated
- Used amount update saat loan disbursed

---

### 5. [**LoanService.java**](src/main/java/com/example/genggamin/service/LoanService.java)

**Fungsi**: Business logic untuk loan management.

**Methods**:

- `createLoan()`: Customer mengajukan pinjaman
- `getLoanById()`: Get loan by ID
- `getLoansByCustomerId()`: List loans by customer
- `getAllLoans()`: List semua loans
- `approveLoan()`: Approve loan (Officer/Manager)
- `rejectLoan()`: Reject loan
- `disburseLoan()`: Cairkan dana (Manager)
- `cancelLoan()`: Cancel loan

**Business Rules**:

- Cek plafond available sebelum create loan
- Validate amount tidak melebihi available plafond
- Status workflow: PENDING → APPROVED → DISBURSED
- Update plafond used amount saat disburse
- Hanya loan dengan status PENDING yang bisa di-approve/reject

---

### 6. [**EmailService.java**](src/main/java/com/example/genggamin/service/EmailService.java)

**Fungsi**: Service untuk mengirim email.

**Methods**:

- `sendPasswordResetEmail()`: Kirim email reset password
- `sendSimpleEmail()`: Kirim email sederhana
- `sendHtmlEmail()`: Kirim email HTML

**Configuration**:

- SMTP settings dari `application.properties`
- Frontend URL untuk generate reset password link

**Template Email**: HTML email dengan branding dan styling.

---

### 7. [**PasswordResetService.java**](src/main/java/com/example/genggamin/service/PasswordResetService.java)

**Fungsi**: Business logic untuk reset password.

**Methods**:

- `createPasswordResetToken()`: Generate token dan kirim email
- `validatePasswordResetToken()`: Validasi token
- `resetPassword()`: Reset password dengan token
- `deleteExpiredTokens()`: Cleanup token expired

**Workflow**:

1. User request forgot password (masukkan email)
2. System generate token (UUID) dengan expiry 1 jam
3. Kirim email berisi link reset password
4. User klik link, masukkan password baru
5. System validasi token dan reset password
6. Token ditandai sebagai used

**Security**:

- Token expire dalam 1 jam
- Token hanya bisa digunakan sekali
- Token di-hash sebelum disimpan di database

---

### 8. [**TokenBlacklistService.java**](src/main/java/com/example/genggamin/service/TokenBlacklistService.java)

**Fungsi**: Mengelola blacklist JWT token (untuk logout).

**Methods**:

- `blacklistToken()`: Tambahkan token ke blacklist
- `isTokenBlacklisted()`: Cek apakah token di-blacklist
- `removeToken()`: Hapus token dari blacklist (cleanup)

**Implementation**:

- Menggunakan Redis untuk storage
- Token disimpan dengan TTL sesuai expiration time token
- Otomatis expired dari Redis setelah token expired

**Use Case**:

- User logout → token di-blacklist
- Setiap request → cek apakah token di-blacklist
- Jika di-blacklist → reject request (401 Unauthorized)

---

## 🔐 Security Flow

### Authentication Flow:

1. User login dengan username/password
2. `AuthController` validate credentials
3. `UserService.loadUserByUsername()` load user details
4. `JwtUtil.generateToken()` generate JWT token
5. Return token ke client
6. Client simpan token (localStorage/cookies)
7. Setiap request, client kirim token di header: `Authorization: Bearer <token>`

### Authorization Flow:

1. `JwtAuthenticationFilter` extract token dari header
2. Validate token menggunakan `JwtUtil`
3. Cek blacklist menggunakan `TokenBlacklistService`
4. Load user details dan set authentication
5. Spring Security cek role/permission dengan `@PreAuthorize`
6. Jika ada permission → proceed
7. Jika tidak ada permission → `RestAccessDeniedHandler` (403)

---

## 📝 Conventions & Best Practices

### Naming Conventions:

- **Entity**: Singular noun (User, Customer, Loan)
- **Repository**: EntityNameRepository
- **Service**: EntityNameService
- **Controller**: EntityNameController
- **DTO Request**: EntityNameRequest
- **DTO Response**: EntityNameResponse

### Architecture:

- **Controller**: Handle HTTP requests, validasi input, return response
- **Service**: Business logic, transaction management
- **Repository**: Database operations
- **Entity**: Database table mapping
- **DTO**: Data transfer between layers

### Error Handling:

- Custom exceptions untuk business logic errors
- Global exception handler dengan `@ControllerAdvice`
- Consistent error response format

### Caching Strategy:

- User data: 5 menit TTL
- Role data: 10 menit TTL
- Evict cache on update/delete operations

### Soft Delete:

- Customer dan Plafond menggunakan soft delete
- Flag: `isDeleted` boolean
- Keep audit trail untuk compliance

---

## 🚀 Deployment Notes

### Required Environment Variables:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/genggamin
spring.datasource.username=postgres
spring.datasource.password=your_password

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT
jwt.secret=your-secret-key-min-256-bits
jwt.expiration=86400000

# Email
spring.mail.host=smtp.gmail.com
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# Frontend URL
app.frontend.url=http://localhost:3000
```

### Database Migration:

- Schema auto-created by Hibernate (JPA)
- Initial data seeded by `DataInitializer`
- SQL scripts tersedia di `DB.sql` dan `init-db.sql`

---

## 📞 Support

Untuk pertanyaan lebih lanjut, silakan hubungi tim development atau refer ke dokumentasi lainnya:

- `DOCUMENTATION.md` - Panduan lengkap aplikasi
- `RBAC_DOCUMENTATION.md` - Role-Based Access Control
- `REDIS_DOCUMENTATION.md` - Redis caching guide
- `TESTING_GUIDE.md` - Testing guidelines
- `ENDPOINT_DOCUMENTATION.md` - API endpoints reference

---

**Version**: 1.0.0  
**Last Updated**: January 2026  
**Maintained by**: Genggamin Development Team
