-- Script untuk menghapus kolom phone dari tabel users
-- Karena phone diambil dari tabel customers
USE LoanDB;
GO

-- Hapus kolom phone jika ada
IF EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('users') 
    AND name = 'phone'
)
BEGIN
    ALTER TABLE users
    DROP COLUMN phone;
    PRINT '✓ Column phone has been removed from users table';
END
ELSE
BEGIN
    PRINT '✓ Column phone does not exist in users table';
END
GO

-- Verify the column has been removed
PRINT '';
PRINT '=== Verification: Current users table structure ===';
SELECT 
    c.name AS ColumnName,
    t.name AS DataType,
    c.max_length AS MaxLength,
    c.is_nullable AS IsNullable
FROM sys.columns c
INNER JOIN sys.types t ON c.user_type_id = t.user_type_id
WHERE c.object_id = OBJECT_ID('users')
ORDER BY c.column_id;
GO

PRINT '';
PRINT '✓ Script completed successfully!';
GO
