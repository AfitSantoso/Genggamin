# Unit Testing Guide - Auth Login

## 📝 Test Coverage

Unit test untuk `AuthController.login()` mencakup skenario:

1. ✅ **Login berhasil dengan kredensial valid**
2. ❌ **Login gagal dengan kredensial invalid**
3. ❌ **Login gagal dengan user tidak ditemukan**
4. ❌ **Login gagal dengan akun inactive**
5. ❌ **Login gagal dengan username kosong**
6. ❌ **Login gagal dengan password null**
7. ✅ **Login berhasil dengan user yang memiliki multiple roles**

## 🚀 Cara Menjalankan Test

### 1. Run Semua Test di Class AuthControllerTest

```bash
# Menggunakan Maven
mvn test -Dtest=AuthControllerTest

# Atau test semua file
mvn test
```

### 2. Run Test Spesifik (Satu Method)

```bash
# Run hanya test login berhasil
mvn test -Dtest=AuthControllerTest#login_WithValidCredentials_ShouldReturnTokenAndUserInfo

# Run test login gagal
mvn test -Dtest=AuthControllerTest#login_WithInvalidCredentials_ShouldReturnUnauthorized
```

### 3. Run Test dengan Output Detail

```bash
# Dengan detail output
mvn test -Dtest=AuthControllerTest -X

# Dengan coverage report (jika sudah ada JaCoCo)
mvn clean test jacoco:report
```

### 4. Run dari VS Code

1. Buka file `AuthControllerTest.java`
2. Klik **Run Test** di atas nama class atau method
3. Atau klik kanan → **Run Test**

### 5. Run dari IntelliJ IDEA

1. Buka file `AuthControllerTest.java`
2. Klik icon ▶️ hijau di sebelah class/method
3. Atau kanan klik → **Run 'AuthControllerTest'**

## 📊 Hasil Test Output

### Successful Test Output:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.genggamin.controller.AuthControllerTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.5 s
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
```

### Failed Test Output (contoh):

```
[ERROR] Tests run: 7, Failures: 1, Errors: 0, Skipped: 0
[ERROR] login_WithValidCredentials_ShouldReturnTokenAndUserInfo  Time elapsed: 0.5 s  <<< FAILURE!
Expected: 200
Actual: 401
```

## 🔍 Penjelasan Test Cases

### Test 1: Login Berhasil

```java
@Test
void login_WithValidCredentials_ShouldReturnTokenAndUserInfo()
```

- **Input:** Username dan password valid
- **Expected:** Status 200 OK dengan token JWT dan user info
- **Validates:** Flow login normal

### Test 2: Login Gagal - Invalid Credentials

```java
@Test
void login_WithInvalidCredentials_ShouldReturnUnauthorized()
```

- **Input:** Password salah
- **Expected:** Status 401 Unauthorized dengan error message
- **Validates:** Security - password validation

### Test 3: Login Gagal - User Not Found

```java
@Test
void login_WithNonExistentUser_ShouldReturnUnauthorized()
```

- **Input:** Username tidak ada di database
- **Expected:** Status 401 dengan message "User not found"
- **Validates:** User existence check

### Test 4: Login Gagal - Inactive Account

```java
@Test
void login_WithInactiveUser_ShouldReturnUnauthorized()
```

- **Input:** User dengan status inactive
- **Expected:** Status 401 dengan message "Account is inactive"
- **Validates:** Account status check

### Test 5 & 6: Validation Tests

```java
@Test
void login_WithEmptyUsername_ShouldReturnBadRequest()

@Test
void login_WithNullPassword_ShouldReturnBadRequest()
```

- **Input:** Data tidak lengkap
- **Expected:** Status 401/400
- **Validates:** Input validation

### Test 7: Multiple Roles

```java
@Test
void login_WithValidCredentialsAndMultipleRoles_ShouldReturnToken()
```

- **Input:** User dengan multiple roles (USER + ADMIN)
- **Expected:** Token berhasil generate dengan semua roles
- **Validates:** Role collection dalam JWT

## 🛠️ Struktur Test

### Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### Annotations Used

- `@WebMvcTest(AuthController.class)` - Load only AuthController
- `@MockBean` - Mock dependencies (UserService, JwtUtil, etc.)
- `@Autowired MockMvc` - For HTTP request testing
- `@BeforeEach` - Setup test data before each test
- `@Test` - Mark test methods

### Mock Objects

```java
@MockBean private UserService userService;
@MockBean private JwtUtil jwtUtil;
@MockBean private TokenBlacklistService tokenBlacklistService;
@MockBean private PasswordResetService passwordResetService;
```

## 🐛 Troubleshooting

### Error: "No tests were executed"

**Solusi:**

```bash
# Rebuild project
mvn clean install
mvn test
```

### Error: "MockBean could not be found"

**Solusi:** Pastikan dependency spring-boot-starter-test ada di pom.xml

### Error: "Method not found in controller"

**Solusi:**

1. Pastikan controller method public
2. Pastikan mapping URL benar
3. Rebuild project

### Test timeout atau terlalu lama

**Solusi:**

```bash
# Skip integration tests, run unit tests only
mvn test -DskipITs
```

## 📈 Best Practices

### 1. Test Naming Convention

```
methodName_StateUnderTest_ExpectedBehavior
```

Contoh: `login_WithValidCredentials_ShouldReturnTokenAndUserInfo`

### 2. AAA Pattern

```java
@Test
void testName() {
    // Arrange - Setup test data

    // Act - Execute the method

    // Assert - Verify results
}
```

### 3. Mock Return Values

```java
when(userService.authenticate(any())).thenReturn(testUser);
```

### 4. Verify Behavior

```java
verify(userService, times(1)).authenticate(any());
```

## 📋 Quick Commands Cheat Sheet

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthControllerTest

# Run specific test method
mvn test -Dtest=AuthControllerTest#login_WithValidCredentials_ShouldReturnTokenAndUserInfo

# Run tests with detailed output
mvn test -X

# Run tests and skip if compilation fails
mvn test -Dmaven.test.failure.ignore=true

# Clean and test
mvn clean test

# Test with coverage (if JaCoCo configured)
mvn clean test jacoco:report
```

## 🎯 Next Steps

1. **Add more test cases** untuk register, logout, dll
2. **Add integration tests** untuk test dengan real database
3. **Add coverage report** menggunakan JaCoCo
4. **Setup GitHub Actions** untuk auto-run tests on push

## 📚 Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [MockMvc Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#spring-mvc-test-framework)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

---

**Happy Testing! 🚀**
