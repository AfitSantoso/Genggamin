package com.example.genggamin.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service untuk mengelola blacklist JWT token menggunakan Redis
 * Token yang di-blacklist akan disimpan di Redis dengan TTL sesuai expiration time token
 */
@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    
    private final RedisTemplate<String, Object> redisTemplate;

    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Menambahkan token ke blacklist dengan TTL
     * @param token JWT token yang akan di-blacklist
     * @param expirationTimeMillis Waktu expirasi token dalam milliseconds
     */
    public void blacklistToken(String token, long expirationTimeMillis) {
        String key = BLACKLIST_PREFIX + token;
        long currentTimeMillis = System.currentTimeMillis();
        
        // Hitung sisa waktu hingga token expired
        long ttlMillis = expirationTimeMillis - currentTimeMillis;
        
        // Jika token sudah expired, tidak perlu di-blacklist
        if (ttlMillis > 0) {
            // Simpan token di Redis dengan TTL
            redisTemplate.opsForValue().set(key, "blacklisted", ttlMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Memeriksa apakah token ada di blacklist
     * @param token JWT token yang akan dicek
     * @return true jika token di-blacklist, false jika tidak
     */
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean hasKey = redisTemplate.hasKey(key);
        return hasKey != null && hasKey;
    }

    /**
     * Menghapus token dari blacklist (opsional, biasanya tidak diperlukan karena TTL)
     * @param token JWT token yang akan dihapus dari blacklist
     */
    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
    }

    /**
     * Menghapus semua token dari blacklist (untuk testing/maintenance)
     */
    public void clearAllBlacklist() {
        redisTemplate.keys(BLACKLIST_PREFIX + "*")
            .forEach(key -> redisTemplate.delete(key));
    }
}
