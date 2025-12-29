# Customer Profile - Security & Access Control

## ✅ Keamanan Sudah Terjamin

### 1. **Authentication Required**

Semua endpoint customer profile memerlukan JWT Token yang valid:

```java
@RestController
@RequestMapping("/customers")
public class CustomerController {
    // Semua endpoint di sini WAJIB authenticated
}
```

**SecurityConfig.java** sudah mengatur:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**", "/h2-console/**").permitAll()
    .anyRequest().authenticated()  // ← Semua request lain WAJIB login
)
```

### 2. **User Isolation - Hanya Bisa Akses Data Sendiri**

Setiap endpoint mengambil user ID dari token JWT yang sedang login:

```java
@PostMapping("/profile")
public ResponseEntity<ApiResponse<CustomerResponse>> createOrUpdateProfile(
        @RequestBody CustomerRequest request) {
    // Ambil user dari token, BUKAN dari request body
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Long userId = extractUserIdFromAuthentication(authentication);

    // User hanya bisa create/update profile mereka sendiri
    CustomerResponse response = customerService.createOrUpdateCustomer(userId, request);
    return ResponseEntity.ok(response);
}
```

**User TIDAK BISA**:

- ❌ Akses profile user lain
- ❌ Update profile user lain
- ❌ Create profile untuk user lain

**User HANYA BISA**:

- ✅ Create/update profile mereka sendiri
- ✅ View profile mereka sendiri
- ✅ Cek status profile mereka sendiri

### 3. **Validation di Backend**

Service layer melakukan validasi:

- NIK unique validation
- NIK tidak bisa digunakan oleh customer lain
- User harus exist di database
- Emergency contacts di-validate sebelum disimpan

### 4. **Endpoint Protection Summary**

| Endpoint                 | Method | Auth Required | Who Can Access                 |
| ------------------------ | ------ | ------------- | ------------------------------ |
| `/customers/profile`     | POST   | ✅ Yes        | User yang login (data sendiri) |
| `/customers/profile`     | GET    | ✅ Yes        | User yang login (data sendiri) |
| `/customers/has-profile` | GET    | ✅ Yes        | User yang login (data sendiri) |

### 5. **JWT Token Flow**

```
1. User Login → Dapat JWT Token
2. Token berisi: username, roles, expiration
3. Setiap request ke /customers/* → Must include token in header
4. JwtAuthenticationFilter validate token
5. Extract username from token
6. Load user dari database
7. Set SecurityContext
8. Controller extract userId dari SecurityContext
9. Service hanya process data untuk userId tersebut
```

### 6. **Tidak Ada Role-Based Access**

Endpoint customer profile **TIDAK** dibatasi berdasarkan role (CUSTOMER, MARKETING, dll).

**Alasannya:**

- Semua user yang login (apapun rolenya) bisa mengisi profile customer mereka
- System secara otomatis isolate data berdasarkan userId dari token
- User dengan role MARKETING tetap bisa mengisi profile customer jika mereka mau apply loan

**Jika ingin restrict hanya untuk role CUSTOMER:**

```java
@PreAuthorize("hasRole('CUSTOMER')")
@PostMapping("/profile")
public ResponseEntity<ApiResponse<CustomerResponse>> createOrUpdateProfile(...) {
    // ...
}
```

## ⚠️ Potential Security Concerns (Sudah Handled)

### ❌ TIDAK ADA: Akses Profile User Lain

```java
// INI TIDAK MUNGKIN TERJADI karena userId diambil dari token, bukan dari request
GET /customers/profile?userId=123  // ❌ Parameter userId diabaikan
```

### ❌ TIDAK ADA: Bypass Authentication

```java
// Semua endpoint /customers/* sudah protected oleh SecurityFilterChain
// Kalau tidak ada token atau token invalid → 401 Unauthorized
```

### ❌ TIDAK ADA: NIK Duplication

```java
// Service melakukan validasi NIK unique
customerRepository.findByNik(request.getNik()).ifPresent(existingCustomer -> {
    if (!existingCustomer.getUserId().equals(userId)) {
        throw new RuntimeException("NIK already registered by another customer");
    }
});
```

## 🔐 Best Practices Applied

1. ✅ **Authentication First**: Semua endpoint protected
2. ✅ **User Isolation**: User ID dari JWT token, bukan request
3. ✅ **Stateless**: JWT-based, no session
4. ✅ **Input Validation**: NIK, phone format validation
5. ✅ **Database Constraints**: Unique constraints di database level
6. ✅ **Error Handling**: Proper error messages tanpa expose internal details
7. ✅ **CSRF Protection**: Disabled karena stateless JWT
8. ✅ **Transaction Management**: @Transactional untuk data consistency

## 📋 Testing Security

### Test 1: Without Token

```bash
curl -X GET http://localhost:8080/customers/profile
# Expected: 401 Unauthorized
```

### Test 2: With Invalid Token

```bash
curl -X GET http://localhost:8080/customers/profile \
  -H "Authorization: Bearer invalid_token_here"
# Expected: 401 Unauthorized
```

### Test 3: With Valid Token (Success)

```bash
curl -X GET http://localhost:8080/customers/profile \
  -H "Authorization: Bearer <valid_jwt_token>"
# Expected: 200 OK with customer data
```

### Test 4: Try to Access Another User's Data

```bash
# User A login → get token_A
# User B login → get token_B
# User A tries to get User B's data using token_B
# RESULT: User A akan tetap dapat data User A, bukan User B
# Karena userId diambil dari token, bukan dari parameter
```

## 📝 Summary

**Jawaban untuk pertanyaan Anda:**

1. **Apakah hanya CUSTOMER yang bisa create/add data customer?**

   - ❌ Tidak ada restriction berdasarkan role
   - ✅ Semua user yang login bisa mengisi profile customer mereka
   - ✅ Jika ingin restrict, tambahkan `@PreAuthorize("hasRole('CUSTOMER')")`

2. **Apakah hanya user yang login atau ada tokennya yang bisa update/create/get?**
   - ✅ **YA!** Token JWT wajib untuk semua endpoint
   - ✅ User hanya bisa akses/update data mereka sendiri
   - ✅ UserId diambil dari token, bukan dari request body/parameter
   - ✅ Tidak ada cara untuk user mengakses data user lain

**Kesimpulan: Security sudah AMAN! ✅**
