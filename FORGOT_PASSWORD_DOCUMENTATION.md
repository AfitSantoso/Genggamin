# Forgot Password Feature - Genggamin Loan System

## 📧 Fitur yang Sudah Diimplementasikan

Fitur Forgot Password telah berhasil dibuat dengan alur lengkap:

### **Alur Proses:**

1. User request reset password dengan email
2. Sistem generate token dan simpan ke database
3. Sistem kirim email berisi link reset password
4. User klik link atau gunakan token untuk set password baru
5. Token otomatis expire dalam 1 jam
6. Token hanya bisa digunakan sekali

---

## 🗂️ File yang Dibuat

### 1. **Entity**

- `PasswordResetToken.java` - Entity untuk tabel password_reset_tokens

### 2. **Repository**

- `PasswordResetTokenRepository.java` - Repository dengan query custom

### 3. **DTO**

- `ForgotPasswordRequest.java` - Request untuk forgot password
- `ResetPasswordRequest.java` - Request untuk reset password

### 4. **Service**

- `EmailService.java` - Service untuk kirim email menggunakan Spring Mail
- `PasswordResetService.java` - Service untuk handle forgot password logic

### 5. **Controller**

- Update `AuthController.java` dengan 2 endpoint baru:
  - `POST /auth/forgot-password`
  - `POST /auth/reset-password`

---

## 🔧 Konfigurasi Email (WAJIB!)

### **Setup Gmail App Password:**

1. **Login ke Gmail** yang akan digunakan
2. **Aktifkan 2-Step Verification:**

   - Buka: https://myaccount.google.com/security
   - Scroll ke "2-Step Verification" → Aktifkan

3. **Buat App Password:**

   - Buka: https://myaccount.google.com/apppasswords
   - Pilih app: "Mail"
   - Pilih device: "Other" → ketik "Genggamin"
   - Copy password 16 karakter yang dihasilkan

4. **Update application.yml:**

```yaml
spring:
  mail:
    username: your-email@gmail.com # Email Gmail Anda
    password: xxxx xxxx xxxx xxxx # App Password 16 digit
```

### **Alternatif untuk Development:**

Jika tidak ingin setup email dulu, bisa test dengan melihat token di database:

```sql
SELECT TOP 1 * FROM password_reset_tokens
WHERE user_id = (SELECT id FROM users WHERE email = 'user@example.com')
ORDER BY created_at DESC;
```

---

## 📝 API Endpoints

### **1. Forgot Password**

```http
POST /auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}
```

**Response Success:**

```json
{
  "success": true,
  "message": "Link reset password telah dikirim ke email Anda. Silakan cek inbox atau spam folder."
}
```

**Response Error:**

```json
{
  "success": false,
  "message": "User dengan email user@example.com tidak ditemukan"
}
```

---

### **2. Reset Password**

```http
POST /auth/reset-password
Content-Type: application/json

{
  "token": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "newPassword": "newpassword123"
}
```

**Response Success:**

```json
{
  "success": true,
  "message": "Password berhasil direset. Silakan login dengan password baru Anda."
}
```

**Response Error:**

```json
{
  "success": false,
  "message": "Token tidak valid atau tidak ditemukan"
}
```

---

## 🧪 Cara Testing

### **1. Test Forgot Password (Tanpa Email Setup)**

```bash
# 1. Request reset password
curl -X POST http://localhost:8080/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@gmail.com\"}"

# 2. Ambil token dari database
# Buka SQL Server dan jalankan:
SELECT TOP 1 token FROM password_reset_tokens
WHERE user_id = (SELECT id FROM users WHERE email = 'admin@gmail.com')
ORDER BY created_at DESC;

# 3. Reset password dengan token
curl -X POST http://localhost:8080/auth/reset-password \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"TOKEN_DARI_DATABASE\",\"newPassword\":\"newpass123\"}"

# 4. Login dengan password baru
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"newpass123\"}"
```

### **2. Test dengan Email (Setelah Setup Gmail)**

1. Request forgot password di Postman/cURL
2. Cek email (inbox atau spam)
3. Copy token dari email
4. Test reset password dengan token tersebut

---

## ✨ Fitur Tambahan

### **1. Auto Cleanup Token**

Token yang expired atau sudah digunakan akan dibersihkan otomatis setiap hari jam 2 pagi.

### **2. Validasi Token**

- Token hanya valid 1 jam
- Token hanya bisa digunakan 1x
- Token lama otomatis di-invalidate saat request baru

### **3. Security**

- Password minimal 6 karakter
- Password di-hash dengan BCrypt
- Token menggunakan UUID (random dan unik)
- Email HTML template dengan styling

### **4. Email Template**

Email yang dikirim berisi:

- Nama user
- Link reset password (untuk frontend)
- Token (untuk testing API)
- Warning expiry time
- Branding Genggamin

---

## 🚀 Next Steps

1. **Setup Email:**

   - Ikuti panduan di atas untuk setup Gmail App Password
   - Update `application.yml` dengan credential Anda

2. **Test:**

   - Test forgot password endpoint
   - Cek apakah email terkirim
   - Test reset password dengan token

3. **Frontend (Opsional):**

   - Buat halaman reset password di frontend
   - Parse token dari URL query parameter
   - Submit form reset password ke API

4. **Improvement:**
   - Rate limiting untuk mencegah spam
   - Notifikasi ke user setelah password berhasil direset
   - Log audit untuk security

---

## 📊 Database Schema

```sql
CREATE TABLE password_reset_tokens (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expired_at DATETIME2 NOT NULL,
    used BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 📌 Important Notes

- Token berlaku **1 jam** sejak dibuat
- Token **hanya bisa digunakan 1x**
- Request forgot password baru akan **invalidate token lama**
- Email menggunakan **HTML template** yang responsive
- Endpoint **tidak memerlukan authentication** (public)

---

## 🔍 Troubleshooting

### Email tidak terkirim?

1. Cek apakah sudah setup Gmail App Password
2. Cek log aplikasi untuk error message
3. Pastikan internet connection aktif
4. Cek spam folder di email

### Token invalid?

1. Cek apakah token sudah expired (>1 jam)
2. Cek apakah token sudah pernah digunakan
3. Cek database: `SELECT * FROM password_reset_tokens WHERE token = 'xxx'`

### User not found?

1. Pastikan email yang diinput benar dan ada di database
2. Cek: `SELECT * FROM users WHERE email = 'xxx'`

---

**✅ Feature COMPLETE! Ready to use!**
