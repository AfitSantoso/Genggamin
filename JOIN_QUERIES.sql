-- ====================================================================
-- SQL SCRIPT UNTUK JOIN TABLE LOANS, LOAN_REVIEWS, DAN LOAN_APPROVALS
-- ====================================================================

-- 1. JOIN LOANS, LOAN_REVIEWS, dan CUSTOMERS (Untuk Marketing)
-- Endpoint: GET /loans/reviewed
-- Menampilkan loans yang sudah direview dengan info customer
-- ====================================================================
SELECT 
    -- Loan Info
    l.id AS loan_id,
    l.loan_amount,
    l.tenor_month,
    l.interest_rate,
    l.purpose,
    l.status AS loan_status,
    l.submission_date,
    l.created_at,
    l.updated_at,
    -- Customer Info
    c.id AS customer_id,
    c.full_name AS customer_name,
    c.email AS customer_email,
    c.phone AS customer_phone,
    c.monthly_income,
    c.occupation,
    -- Review Info
    lr.id AS review_id,
    lr.reviewed_by,
    lr.review_notes,
    lr.review_status,
    lr.reviewed_at
FROM 
    LoanDB.dbo.loans l
INNER JOIN 
    LoanDB.dbo.loan_reviews lr ON l.id = lr.loan_id
INNER JOIN 
    LoanDB.dbo.customers c ON l.customer_id = c.id
ORDER BY 
    l.updated_at DESC;

-- ====================================================================
-- 2. JOIN LOANS dengan LOAN_APPROVALS (Data yang sudah di-approve/reject)
-- Endpoint: GET /loans/approved
-- ====================================================================
SELECT 
    l.id AS loan_id,
    l.customer_id,
    l.plafond_id,
    l.loan_amount,
    l.tenor_month,
    l.interest_rate,
    l.purpose,
    l.status AS loan_status,
    l.submission_date,
    l.created_at,
    l.updated_at,
    la.id AS approval_id,
    la.approved_by,
    la.approval_status,
    la.approval_notes,
    la.approved_at
FROM 
    LoanDB.dbo.loans l
INNER JOIN 
    LoanDB.dbo.loan_approvals la ON l.id = la.loan_id
ORDER BY 
    l.updated_at DESC;

-- ====================================================================
-- 3. JOIN LENGKAP (LOANS + LOAN_REVIEWS + LOAN_APPROVALS)
-- Full workflow tracking dari submit sampai approve
-- ====================================================================
SELECT 
    l.id AS loan_id,
    l.customer_id,
    l.plafond_id,
    l.loan_amount,
    l.tenor_month,
    l.interest_rate,
    l.purpose,
    l.status AS loan_status,
    l.submission_date,
    l.created_at,
    l.updated_at,
    -- Review data
    lr.id AS review_id,
    lr.reviewed_by,
    lr.review_notes,
    lr.review_status,
    lr.reviewed_at,
    -- Approval data
    la.id AS approval_id,
    la.approved_by,
    la.approval_status,
    la.approval_notes,
    la.approved_at
FROM 
    LoanDB.dbo.loans l
LEFT JOIN 
    LoanDB.dbo.loan_reviews lr ON l.id = lr.loan_id
LEFT JOIN 
    LoanDB.dbo.loan_approvals la ON l.id = la.loan_id
ORDER BY 
    l.updated_at DESC;

-- ====================================================================
-- 4. JOIN dengan CUSTOMER INFO (Include customer name)
-- ====================================================================
SELECT 
    l.id AS loan_id,
    c.full_name AS customer_name,
    c.monthly_income,
    l.plafond_id,
    l.loan_amount,
    l.tenor_month,
    l.interest_rate,
    l.purpose,
    l.status AS loan_status,
    l.submission_date,
    -- Review info
    lr.review_status,
    lr.review_notes,
    lr.reviewed_at,
    -- Approval info
    la.approval_status,
    la.approval_notes,
    la.approved_at
FROM 
    LoanDB.dbo.loans l
INNER JOIN 
    LoanDB.dbo.customers c ON l.customer_id = c.id
LEFT JOIN 
    LoanDB.dbo.loan_reviews lr ON l.id = lr.loan_id
LEFT JOIN 
    LoanDB.dbo.loan_approvals la ON l.id = la.loan_id
ORDER BY 
    l.submission_date DESC;

-- ====================================================================
-- 5. STATISTIK REVIEW & APPROVAL
-- ====================================================================
SELECT 
    l.status AS loan_status,
    COUNT(*) AS total_loans,
    COUNT(lr.id) AS reviewed_count,
    COUNT(la.id) AS approved_count,
    SUM(l.loan_amount) AS total_amount
FROM 
    LoanDB.dbo.loans l
LEFT JOIN 
    LoanDB.dbo.loan_reviews lr ON l.id = lr.loan_id
LEFT JOIN 
    LoanDB.dbo.loan_approvals la ON l.id = la.loan_id
GROUP BY 
    l.status;

-- ====================================================================
-- 6. LOANS dengan REVIEWER dan APPROVER NAMES
-- Join dengan users table untuk mendapat nama reviewer & approver
-- ====================================================================
SELECT 
    l.id AS loan_id,
    l.loan_amount,
    l.tenor_month,
    l.status AS loan_status,
    l.submission_date,
    -- Reviewer info
    u_reviewer.username AS reviewed_by_username,
    lr.review_status,
    lr.review_notes,
    lr.reviewed_at,
    -- Approver info
    u_approver.username AS approved_by_username,
    la.approval_status,
    la.approval_notes,
    la.approved_at
FROM 
    LoanDB.dbo.loans l
LEFT JOIN 
    LoanDB.dbo.loan_reviews lr ON l.id = lr.loan_id
LEFT JOIN 
    LoanDB.dbo.users u_reviewer ON lr.reviewed_by = u_reviewer.id
LEFT JOIN 
    LoanDB.dbo.loan_approvals la ON l.id = la.loan_id
LEFT JOIN 
    LoanDB.dbo.users u_approver ON la.approved_by = u_approver.id
ORDER BY 
    l.submission_date DESC;

-- ====================================================================
-- 7. FIND SPECIFIC LOAN with ALL DETAILS
-- Ganti @loan_id dengan ID loan yang ingin dicari
-- ====================================================================
DECLARE @loan_id BIGINT = 1;

SELECT 
    -- Loan basic info
    l.id AS loan_id,
    l.customer_id,
    c.full_name AS customer_name,
    c.email AS customer_email,
    c.phone AS customer_phone,
    l.plafond_id,
    l.loan_amount,
    l.tenor_month,
    l.interest_rate,
    l.purpose,
    l.status AS loan_status,
    l.submission_date,
    l.created_at,
    l.updated_at,
    -- Review details
    lr.id AS review_id,
    u_reviewer.username AS reviewer_username,
    lr.review_status,
    lr.review_notes,
    lr.reviewed_at,
    -- Approval details
    la.id AS approval_id,
    u_approver.username AS approver_username,
    la.approval_status,
    la.approval_notes,
    la.approved_at
FROM 
    LoanDB.dbo.loans l
INNER JOIN 
    LoanDB.dbo.customers c ON l.customer_id = c.id
LEFT JOIN 
    LoanDB.dbo.loan_reviews lr ON l.id = lr.loan_id
LEFT JOIN 
    LoanDB.dbo.users u_reviewer ON lr.reviewed_by = u_reviewer.id
LEFT JOIN 
    LoanDB.dbo.loan_approvals la ON l.id = la.loan_id
LEFT JOIN 
    LoanDB.dbo.users u_approver ON la.approved_by = u_approver.id
WHERE 
    l.id = @loan_id;

-- ====================================================================
-- 8. CHECK DATA CONSISTENCY
-- Pastikan tidak ada missing review/approval
-- ====================================================================
-- Loans yang status UNDER_REVIEW tapi tidak ada di loan_reviews
SELECT 
    l.id,
    l.status,
    'Missing review record' AS issue
FROM 
    LoanDB.dbo.loans l
LEFT JOIN 
    LoanDB.dbo.loan_reviews lr ON l.id = lr.loan_id
WHERE 
    l.status = 'UNDER_REVIEW' 
    AND lr.id IS NULL

UNION ALL

-- Loans yang status APPROVED tapi tidak ada di loan_approvals
SELECT 
    l.id,
    l.status,
    'Missing approval record' AS issue
FROM 
    LoanDB.dbo.loans l
LEFT JOIN 
    LoanDB.dbo.loan_approvals la ON l.id = la.loan_id
WHERE 
    l.status = 'APPROVED' 
    AND la.id IS NULL;

-- ====================================================================
-- 9. LOANS SUMMARY by STATUS with JOIN COUNT
-- ====================================================================
SELECT 
    l.status,
    COUNT(DISTINCT l.id) AS total_loans,
    COUNT(DISTINCT lr.id) AS has_review,
    COUNT(DISTINCT la.id) AS has_approval,
    AVG(l.loan_amount) AS avg_loan_amount,
    MIN(l.loan_amount) AS min_loan_amount,
    MAX(l.loan_amount) AS max_loan_amount
FROM 
    LoanDB.dbo.loans l
LEFT JOIN 
    LoanDB.dbo.loan_reviews lr ON l.id = lr.loan_id
LEFT JOIN 
    LoanDB.dbo.loan_approvals la ON l.id = la.loan_id
GROUP BY 
    l.status
ORDER BY 
    l.status;

-- ====================================================================
-- 10. RECENT ACTIVITY (Last 10 actions)
-- ====================================================================
SELECT TOP 10
    l.id AS loan_id,
    l.status,
    l.loan_amount,
    CASE 
        WHEN la.approved_at IS NOT NULL THEN 'APPROVAL'
        WHEN lr.reviewed_at IS NOT NULL THEN 'REVIEW'
        ELSE 'SUBMISSION'
    END AS last_action,
    COALESCE(la.approved_at, lr.reviewed_at, l.submission_date) AS action_time,
    COALESCE(u_approver.username, u_reviewer.username, c.full_name) AS action_by
FROM 
    LoanDB.dbo.loans l
LEFT JOIN 
    LoanDB.dbo.customers c ON l.customer_id = c.id
LEFT JOIN 
    LoanDB.dbo.loan_reviews lr ON l.id = lr.loan_id
LEFT JOIN 
    LoanDB.dbo.users u_reviewer ON lr.reviewed_by = u_reviewer.id
LEFT JOIN 
    LoanDB.dbo.loan_approvals la ON l.id = la.loan_id
LEFT JOIN 
    LoanDB.dbo.users u_approver ON la.approved_by = u_approver.id
ORDER BY 
    action_time DESC;
