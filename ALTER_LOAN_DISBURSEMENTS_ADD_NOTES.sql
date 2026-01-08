-- Script untuk menambahkan kolom notes ke tabel loan_disbursements jika belum ada
USE LoanDB;
GO

-- Check if notes column exists, if not add it
IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('loan_disbursements') 
    AND name = 'notes'
)
BEGIN
    ALTER TABLE loan_disbursements
    ADD notes VARCHAR(1000);
    PRINT 'Column notes added to loan_disbursements table';
END
ELSE
BEGIN
    PRINT 'Column notes already exists in loan_disbursements table';
END
GO
