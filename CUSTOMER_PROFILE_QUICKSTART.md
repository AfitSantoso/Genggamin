# Quick Start Guide - Customer Profile API

## Ringkasan

Endpoint baru untuk mengisi data customer yang diperlukan sebelum mengajukan pinjaman.

## URL Endpoints

- `POST /customers/profile` - Buat/update profil customer
- `GET /customers/profile` - Lihat profil customer
- `GET /customers/has-profile` - Cek apakah sudah ada profil

## Data Yang Dibutuhkan

1. **NIK** (wajib) - 16 digit
2. **Address** - Alamat lengkap
3. **Date of Birth** - Tanggal lahir
4. **Monthly Income** (penting) - Untuk perhitungan plafond
5. **Full Name** - Nama lengkap customer
6. **Phone** - Nomor telepon customer
7. **Current Address** - Alamat domisili saat ini
8. **Mother Maiden Name** - Nama gadis ibu kandung
9. **Emergency Contacts** - Minimal 1 kontak darurat

## Contoh Request

### 1. Buat Profil Customer

```bash
POST http://localhost:8080/customers/profile
Authorization: Bearer <token>
Content-Type: application/json

{
  "nik": "3201234567890123",
  "address": "Jl. Contoh No. 123, Jakarta",
  "dateOfBirth": "1990-01-15",
  "monthlyIncome": 5000000.00,
  "fullName": "John Doe Customer",
  "phone": "081234567890",
  "currentAddress": "Jl. Alamat Sekarang No. 456, Jakarta Pusat",
  "motherMaidenName": "Siti Aminah",
  "emergencyContacts": [
    {
      "contactName": "Budi Santoso",
      "contactPhone": "081234567890",
      "relationship": "ORANG_TUA"
    }
  ]
}
```

### 2. Lihat Profil

```bash
GET http://localhost:8080/customers/profile
Authorization: Bearer <token>
```

### 3. Cek Status Profil

```bash
GET http://localhost:8080/customers/has-profile
Authorization: Bearer <token>
```

## Flow Penggunaan

```
1. Login → Dapat Token
2. Cek has-profile → false?
3. Isi profile (POST)
4. Verifikasi profile (GET)
5. Bisa apply loan
```

## Relationship Options

- `ORANG_TUA` - Orang tua
- `SAUDARA` - Saudara kandung
- `PASANGAN` - Suami/istri
- `TEMAN` - Teman dekat

## Testing

1. Import `Genggamin_RBAC.postman_collection.json` ke Postman
2. Login sebagai CUSTOMER
3. Gunakan folder "Customer Profile"
4. Test ketiga endpoint

## Catatan Penting

- NIK harus unique per customer
- User hanya bisa create/update profile sendiri
- Emergency contacts akan di-replace saat update (bukan append)
- Monthly income penting untuk eligibility plafond

## File Terkait

- **Entity**: `Customer.java`, `EmergencyContact.java`
- **Repository**: `CustomerRepository.java`, `EmergencyContactRepository.java`
- **Service**: `CustomerService.java`
- **Controller**: `CustomerController.java`
- **DTO**: `CustomerRequest.java`, `CustomerResponse.java`, `EmergencyContactRequest.java`, `EmergencyContactResponse.java`
- **Dokumentasi Lengkap**: `CUSTOMER_PROFILE_DOCUMENTATION.md`

## Troubleshooting

### Error: "NIK already registered"

- NIK sudah dipakai customer lain
- Gunakan NIK berbeda

### Error: "User not found"

- Token tidak valid atau expired
- Login ulang

### Error: "Customer data not found"

- Belum ada profile
- Create profile dulu

## Next Steps

Setelah profile lengkap:

1. Cek plafond yang available: `GET /plafonds`
2. Apply untuk loan: `POST /loans/apply`
3. Track status loan: `GET /loans/my-loans`
