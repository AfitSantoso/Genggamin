-- Script untuk menambahkan field full_name ke tabel users
USE LoanDB;
GO

-- Tambah kolom full_name jika belum ada
IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('users') 
    AND name = 'full_name'
)
BEGIN
    ALTER TABLE users
    ADD full_name VARCHAR(150);
    PRINT '✓ Column full_name added to users table';
END
ELSE
BEGIN
    PRINT '✓ Column full_name already exists in users table';
END
GO

-- Verify the column has been added
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
