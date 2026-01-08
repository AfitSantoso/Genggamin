-- Script untuk testing endpoint /loans/disbursed
-- Pastikan tabel loan_disbursements sudah memiliki kolom notes

USE LoanDB;
GO

-- 1. Check if loan_disbursements table exists
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'loan_disbursements')
BEGIN
    PRINT '✓ Table loan_disbursements exists';
    
    -- Check columns
    SELECT 
        c.name AS ColumnName,
        t.name AS DataType,
        c.max_length AS MaxLength,
        c.is_nullable AS IsNullable
    FROM sys.columns c
    INNER JOIN sys.types t ON c.user_type_id = t.user_type_id
    WHERE c.object_id = OBJECT_ID('loan_disbursements')
    ORDER BY c.column_id;
    
    -- Count records
    DECLARE @count INT;
    SELECT @count = COUNT(*) FROM loan_disbursements;
    PRINT 'Total disbursements: ' + CAST(@count AS VARCHAR(10));
END
ELSE
BEGIN
    PRINT '✗ Table loan_disbursements DOES NOT exist';
    PRINT 'Please run DB.sql to create the table';
END
GO

-- 2. Sample query to test JOIN (same as what the endpoint does)
SELECT 
    l.id,
    l.customer_id,
    l.plafond_id,
    l.loan_amount,
    l.tenor_month,
    l.interest_rate,
    l.status,
    l.submission_date,
    l.created_at,
    l.updated_at,
    ld.id AS disbursement_id,
    ld.disbursed_by,
    ld.disbursement_amount,
    ld.disbursement_date,
    ld.bank_account,
    ld.status AS disbursement_status,
    ld.notes
FROM loans l
INNER JOIN loan_disbursements ld ON l.id = ld.loan_id
WHERE l.status = 'DISBURSED';
GO
