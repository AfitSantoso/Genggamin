# Documentation Notes

**JPA annotations (utama):**

- **`@Entity`**: menandakan kelas sebagai entitas JPA yang dipetakan ke tabel database.
- **`@Table`**: menentukan nama tabel SQL yang dipakai untuk entitas (mis. `users`, `roles`).
- **`@Id`**: menandakan primary key kolom.
- **`@GeneratedValue`**: konfigurasi strategi peng-generate-an primary key (mis. `IDENTITY` untuk auto-increment).
- **`@Column`**: konfigurasi kolom (nullable, unique, name, dll.).
- **`@ManyToMany`**: relasi banyak-ke-banyak antar-entitas.
- **`@JoinTable` / `@JoinColumn`**: menentukan tabel join dan nama kolom join untuk relasi Many-to-Many.

**Kenapa terbentuk 3 tabel (`users`, `roles`, `user_roles`)**

Relasi Many-to-Many membuat tabel join tambahan (`user_roles`) untuk menyimpan pasangan `user_id` ↔ `role_id`. Oleh karena itu Hibernate membuat 3 tabel: `users`, `roles`, dan `user_roles`.

**Alur ORM (dari request sampai ke DB)**

1. Client kirim request JSON ke controller (mis. `POST /users`).
2. Controller memetakan JSON ke DTO/Entity dan memutuskan relasi (mengambil Role dari DB jika perlu).
3. Service memanggil `Repository` (Spring Data JPA) untuk menyimpan entitas.
4. Repository meneruskan operasi ke EntityManager Hibernate.
5. Hibernate menerjemahkan perubahan entitas menjadi SQL (INSERT/UPDATE) dan mengirimkannya ke JDBC driver.
6. JDBC driver (SQL Server driver) mengirim SQL ke database SQL Server untuk dieksekusi.

ADMIN : afitadmin
PASSWORD : 12345678
