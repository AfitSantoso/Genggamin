# Docker Deployment Guide - Genggamin Application

## 📋 Prerequisites

- Docker Desktop installed and running
- Docker Compose included with Docker Desktop
- Port 8080, 1433, dan 6379 tidak digunakan aplikasi lain

## 🚀 Step-by-Step Deployment

### Step 1: Build dan Jalankan Semua Services

```bash
# Pastikan di directory project
cd c:\Documents\BCAF\Bootcamp\genggamin

# Build dan start semua containers
docker-compose up -d --build
```

**Penjelasan:**

- `up`: Start containers
- `-d`: Detached mode (run in background)
- `--build`: Build image dari Dockerfile

### Step 2: Monitor Logs

```bash
# Lihat logs semua services
docker-compose logs -f

# Lihat logs aplikasi saja
docker-compose logs -f app

# Lihat logs SQL Server
docker-compose logs -f sqlserver

# Lihat logs Redis
docker-compose logs -f redis
```

Press `Ctrl+C` untuk stop monitoring.

### Step 3: Cek Status Containers

```bash
# Lihat semua running containers
docker-compose ps

# Atau
docker ps
```

Anda harus melihat 3 containers running:

- `genggamin-sqlserver` (port 1433)
- `genggamin-redis` (port 6379)
- `genggamin-app` (port 8080)

### Step 4: Wait for Application Ready

Tunggu sampai aplikasi selesai start (biasanya 30-60 detik). Cek logs:

```bash
docker-compose logs -f app
```

Tunggu sampai muncul: `Started GenggaminApplication in X seconds`

### Step 5: Test Application

**A. Test Health:**

```bash
curl http://localhost:8080/api/users
```

**B. Test Redis Cache:**

Di terminal baru, monitor Redis:

```bash
docker exec -it genggamin-redis redis-cli monitor
```

Di Postman atau browser, panggil 2-3 kali:

```
GET http://localhost:8080/api/users
```

Lihat di monitor Redis - hit pertama akan query DB, selanjutnya dari cache.

**C. Lihat Keys di Redis:**

```bash
# Lihat semua keys
docker exec -it genggamin-redis redis-cli keys "*"

# Lihat specific cache
docker exec -it genggamin-redis redis-cli get "users::allUsers"
```

**D. Test Database Connection:**

```bash
# Connect ke SQL Server
docker exec -it genggamin-sqlserver /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "P@ssw0rd123"

# Di SQL prompt, jalankan:
1> SELECT name FROM sys.databases;
2> GO
1> USE LoanDB;
2> GO
1> SELECT * FROM users;
2> GO
1> EXIT
```

### Step 6: Test Full CRUD Operations

**Login:**

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}
```

**Get Users (akan di-cache):**

```http
GET http://localhost:8080/api/users
Authorization: Bearer <your-token>
```

**Clear Cache dan Test Lagi:**

```bash
# Clear Redis cache
docker exec -it genggamin-redis redis-cli flushall

# Test API lagi - akan lambat karena hit DB
```

## 🛠️ Useful Commands

### Container Management

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: hapus data!)
docker-compose down -v

# Restart specific service
docker-compose restart app

# Rebuild aplikasi setelah code changes
docker-compose up -d --build app

# View resource usage
docker stats
```

### Debugging

```bash
# Enter container shell
docker exec -it genggamin-app sh

# Check environment variables
docker exec genggamin-app env

# Check network
docker network ls
docker network inspect genggamin_genggamin-network
```

### Cleanup

```bash
# Stop dan remove containers
docker-compose down

# Remove images
docker rmi genggamin-app

# Remove all unused Docker resources
docker system prune -a
```

## 🔍 Troubleshooting

### Problem: Container keeps restarting

**Solution:**

```bash
# Check logs
docker-compose logs app

# Common issues:
# 1. Database not ready - wait longer
# 2. Wrong credentials - check docker-compose.yml
# 3. Port already in use - check with: netstat -ano | findstr :8080
```

### Problem: Cannot connect to database

**Solution:**

```bash
# Check SQL Server is healthy
docker exec genggamin-sqlserver /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "P@ssw0rd123" -Q "SELECT 1"

# Check database exists
docker exec genggamin-sqlserver /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "P@ssw0rd123" -Q "SELECT name FROM sys.databases"
```

### Problem: Redis not caching

**Solution:**

```bash
# Test Redis connection
docker exec -it genggamin-redis redis-cli ping

# Check cache configuration in logs
docker-compose logs app | grep -i redis

# Manual test Redis
docker exec -it genggamin-redis redis-cli
127.0.0.1:6379> SET test "Hello"
127.0.0.1:6379> GET test
127.0.0.1:6379> EXIT
```

### Problem: Port already in use

**Solution:**

```bash
# On Windows, find process using port
netstat -ano | findstr :8080
netstat -ano | findstr :1433
netstat -ano | findstr :6379

# Kill process (replace PID with actual process ID)
taskkill /PID <PID> /F

# Or change port in docker-compose.yml:
# ports:
#   - "8081:8080"  # Change 8081 to any available port
```

## 📊 Architecture

```
┌─────────────────┐
│   Postman/API   │
└────────┬────────┘
         │ :8080
         ▼
┌─────────────────┐       ┌─────────────┐
│  Spring Boot    │──────▶│   Redis     │
│      App        │       │   :6379     │
└────────┬────────┘       └─────────────┘
         │
         │ :1433
         ▼
┌─────────────────┐
│   SQL Server    │
│    Database     │
└─────────────────┘
```

## 🎯 Cache Testing Checklist

- [ ] Redis container running dan healthy
- [ ] Application connected ke Redis (cek logs)
- [ ] First API call lambat (hit database)
- [ ] Second API call cepat (hit cache)
- [ ] Cache keys visible di `redis-cli keys "*"`
- [ ] Cache expires after TTL (10 minutes for users)
- [ ] Cache cleared after create/update operations

## 📝 Notes

1. **Data Persistence:** Data disimpan di Docker volumes. Untuk reset:

   ```bash
   docker-compose down -v
   ```

2. **Password SQL Server:** Default password adalah `P@ssw0rd123`. Bisa diubah di `docker-compose.yml` environment variables.

3. **Development Mode:** Untuk development, gunakan:

   ```bash
   docker-compose up  # Tanpa -d untuk lihat logs real-time
   ```

4. **Production:** Untuk production, tambahkan:
   - Environment-specific configurations
   - Secrets management
   - Health check endpoints
   - Resource limits

## ✅ Success Indicators

Aplikasi berhasil jalan jika:

- ✅ Semua 3 containers status "Up (healthy)"
- ✅ Aplikasi log menunjukkan "Started GenggaminApplication"
- ✅ API response success di `http://localhost:8080/api/users`
- ✅ Redis keys muncul setelah API call pertama
- ✅ API call kedua lebih cepat (dari cache)

Selamat! Aplikasi Anda sekarang running di Docker! 🎉
