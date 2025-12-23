-- ====================================================
-- SQL Script: Tambah Kolom Soft Delete ke Tabel Plafonds
-- ====================================================
-- Jalankan script ini di SQL Server sebelum restart aplikasi

USE [your_database_name];
GO

-- Tambahkan kolom untuk soft delete
ALTER TABLE plafonds
ADD is_deleted BIT NOT NULL DEFAULT 0;

ALTER TABLE plafonds
ADD deleted_at DATETIME2 NULL;

ALTER TABLE plafonds
ADD deleted_by VARCHAR(100) NULL;

-- Tambahkan kolom timestamps
ALTER TABLE plafonds
ADD created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME();

ALTER TABLE plafonds
ADD updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME();

GO

-- Verifikasi struktur tabel
EXEC sp_help 'plafonds';

-- Tampilkan semua kolom
SELECT 
    c.name AS ColumnName,
    t.name AS DataType,
    c.max_length AS MaxLength,
    c.is_nullable AS IsNullable,
    dc.definition AS DefaultValue
FROM sys.columns c
INNER JOIN sys.types t ON c.user_type_id = t.user_type_id
LEFT JOIN sys.default_constraints dc ON c.default_object_id = dc.object_id
WHERE c.object_id = OBJECT_ID('plafonds')
ORDER BY c.column_id;

GO

-- Tampilkan data plafonds (untuk verifikasi)
SELECT * FROM plafonds;
