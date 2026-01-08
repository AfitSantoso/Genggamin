-- Script untuk menghapus kolom full_name dari tabel customers
-- Karena full_name diambil dari tabel users
USE LoanDB;
GO

-- Hapus kolom full_name jika ada
IF EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('customers') 
    AND name = 'full_name'
)
BEGIN
    ALTER TABLE customers
    DROP COLUMN full_name;
    PRINT '✓ Column full_name has been removed';
END
ELSE
BEGIN
    PRINT '✓ Column full_name does not exist';
END
GO

-- Verify the column has been removed
PRINT '';
PRINT '=== Verification: Current customers table structure ===';
SELECT 
    c.name AS ColumnName,
    t.name AS DataType,
    c.max_length AS MaxLength,
    c.is_nullable AS IsNullable
FROM sys.columns c
INNER JOIN sys.types t ON c.user_type_id = t.user_type_id
WHERE c.object_id = OBJECT_ID('customers')
ORDER BY c.column_id;
GO

PRINT '';
PRINT '✓ Script completed successfully!';
GO
