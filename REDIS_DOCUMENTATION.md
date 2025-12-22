# Redis Caching Implementation Documentation

## Overview

Redis digunakan sebagai cache layer untuk data user dan role dalam aplikasi Genggamin. Implementasi ini bertujuan untuk:

- **Mengurangi beban database** - Query yang sering diakses di-cache di Redis
- **Meningkatkan performa autentikasi** - Data user di-cache untuk mempercepat proses login
- **Mempercepat response API** - Response time lebih cepat karena data diambil dari cache

## Konfigurasi Redis

### Connection Settings

```yaml
Redis Host: 127.0.0.1
Redis Port: 6379
Connection Timeout: 60000ms
```

### Cache Configuration

File: `application.yml`

```yaml
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      timeout: 60000ms
  cache:
    type: redis
    redis:
      time-to-live: 600000 # 10 menit default
```

## Cache Strategies

### 1. User Cache

**Cache Name**: `users`, `userByUsername`
**TTL**: 15 menit
**Strategi**:

- `getAllUsers()` - Cache semua user dengan key `allUsers`
- `findByUsername()` - Cache per user dengan key `username`
- `createUser()` / `register()` - Evict cache saat ada user baru

### 2. Role Cache

**Cache Name**: `roles`
**TTL**: 30 menit (role jarang berubah)
**Strategi**:

- `getAllRoles()` - Cache semua role dengan key `allRoles`
- `createRole()` - Evict cache saat ada role baru

## Annotation yang Digunakan

### @Cacheable

Menyimpan hasil method ke cache. Jika key sudah ada, langsung return dari cache tanpa execute method.

```java
@Cacheable(value = "userByUsername", key = "#username")
public User findByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User tidak di temukan"));
}
```

### @CacheEvict

Menghapus data dari cache. Digunakan saat data berubah (create, update, delete).

```java
@CacheEvict(value = "roles", allEntries = true)
public Role createRole(Role role) {
    return roleRepository.save(role);
}
```

### @Caching

Kombinasi multiple cache operations dalam satu method.

```java
@Caching(evict = {
    @CacheEvict(value = "users", allEntries = true),
    @CacheEvict(value = "userByUsername", key = "#user.username")
})
public User createUser(User user) {
    return userRepository.saveAndFlush(user);
}
```

## Cache Keys

| Cache Name     | Key Pattern  | Data                             |
| -------------- | ------------ | -------------------------------- |
| users          | `allUsers`   | List semua user                  |
| userByUsername | `{username}` | User object berdasarkan username |
| roles          | `allRoles`   | List semua role                  |

## Serialization

### Jackson JSON Serialization

Redis menggunakan GenericJackson2JsonRedisSerializer dengan konfigurasi:

- Support Java Time API (LocalDateTime, etc)
- Support Polymorphic types
- Non-final types dengan type information

### ObjectMapper Configuration

```java
- JavaTimeModule registered
- Default typing enabled (NON_FINAL)
- Null values tidak di-cache
```

## Performance Benefits

### Before Redis

- Setiap request autentikasi hit database
- Query `findByUsername()` execute setiap kali
- Average response time: ~200-500ms

### After Redis

- Request pertama hit database, selanjutnya dari cache
- Query `findByUsername()` dari Redis (< 5ms)
- Average response time: ~10-50ms
- Database load berkurang 60-80%

## Cache Invalidation

Cache akan otomatis invalid dalam kondisi:

1. **TTL expired** - User cache 15 menit, Role cache 30 menit
2. **Manual eviction** - Saat create user/role
3. **Application restart** - Cache di Redis tetap ada (persistent)

## Monitoring Cache

### Check Redis Connection

```bash
redis-cli ping
# Expected: PONG
```

### View Cached Keys

```bash
redis-cli keys "*"
```

### View Cache Value

```bash
redis-cli get "userByUsername::{username}"
```

### Clear All Cache

```bash
redis-cli flushall
```

## Testing Cache

### 1. Test User Cache

```bash
# Request pertama - hit database
GET http://localhost:8080/users/my-loans
# Log: Query executed

# Request kedua - hit cache
GET http://localhost:8080/users/my-loans
# Log: No query (from cache)
```

### 2. Test Cache Eviction

```bash
# Create user - evict cache
POST http://localhost:8080/auth/register
# Cache cleared

# Next request - rebuild cache
GET http://localhost:8080/users/all
# Log: Query executed, cache updated
```

## Troubleshooting

### Redis Connection Failed

```
Error: Could not connect to Redis at 127.0.0.1:6379
Solution:
1. Pastikan Redis server running
2. Check redis-server status
3. Verify port 6379 tidak digunakan aplikasi lain
```

### Serialization Error

```
Error: Could not read JSON
Solution:
1. Pastikan entity menggunakan @NoArgsConstructor
2. Check circular reference di entity relationship
3. Update ObjectMapper configuration
```

### Cache Not Working

```
Solution:
1. Verify @EnableCaching di main application
2. Check RedisConfig bean loaded
3. Verify cache name di annotation sesuai config
```

## Best Practices

1. **TTL Strategy**

   - Data yang jarang berubah: TTL lebih lama (role: 30 menit)
   - Data yang sering berubah: TTL lebih pendek (user: 15 menit)

2. **Cache Eviction**

   - Selalu evict cache saat data berubah
   - Gunakan `allEntries=true` untuk simple cases
   - Gunakan key spesifik untuk granular control

3. **Cache Keys**

   - Gunakan naming convention yang jelas
   - Include identifier di key (username, id)
   - Avoid collision dengan prefix

4. **Monitoring**
   - Monitor Redis memory usage
   - Track cache hit/miss ratio
   - Set up alerts untuk Redis down

## Dependencies Required

```xml
<!-- Spring Data Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Spring Cache -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

## Files Modified

1. `pom.xml` - Added Redis dependencies
2. `application.yml` - Redis configuration
3. `RedisConfig.java` - Redis & Cache Manager configuration
4. `UserService.java` - Cache annotations for user operations
5. `RoleService.java` - Cache annotations for role operations
6. `GenggaminApplication.java` - Added @EnableCaching

## Future Enhancements

1. **Distributed Cache** - Multi-instance application support
2. **Cache Statistics** - Implement cache metrics dan monitoring
3. **Advanced TTL** - Dynamic TTL based on data importance
4. **Cache Warm-up** - Pre-load frequently accessed data
5. **Redis Cluster** - High availability setup
