-- Script untuk menambahkan field yang missing di tabel customers
USE LoanDB;
GO

-- 1. Add place_of_birth column if not exists
IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('customers') 
    AND name = 'place_of_birth'
)
BEGIN
    ALTER TABLE customers
    ADD place_of_birth VARCHAR(100);
    PRINT '✓ Column place_of_birth added';
END
ELSE
BEGIN
    PRINT '✓ Column place_of_birth already exists';
END
GO

-- 2. Add occupation column if not exists
IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('customers') 
    AND name = 'occupation'
)
BEGIN
    ALTER TABLE customers
    ADD occupation VARCHAR(100);
    PRINT '✓ Column occupation added';
END
ELSE
BEGIN
    PRINT '✓ Column occupation already exists';
END
GO

-- Verify the columns have been added
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
