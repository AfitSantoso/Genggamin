-- SQL Scripts untuk Testing RBAC Implementation

-- Note: Data ini akan ter-generate otomatis oleh DataInitializer.java
-- File ini hanya untuk referensi struktur data

-- Roles yang tersedia:
-- 1. ADMIN - Full access
-- 2. CUSTOMER - Submit loans
-- 3. MARKETING - Review loans
-- 4. BRANCH_MANAGER - Approve/reject loans
-- 5. BACK_OFFICE - Disburse loans

-- Default users:
-- Username: admin, Password: password (ADMIN)
-- Username: customer1, Password: password (CUSTOMER)
-- Username: marketing1, Password: password (MARKETING)
-- Username: manager1, Password: password (BRANCH_MANAGER)
-- Username: backoffice1, Password: password (BACK_OFFICE)

-- Loan Status Flow:
-- SUBMITTED -> UNDER_REVIEW -> APPROVED -> DISBURSED
--                           -> REJECTED

-- Example: Query untuk melihat semua loans dengan status
SELECT 
    l.id,
    u.username as customer,
    l.amount,
    l.tenure_months,
    l.purpose,
    l.status,
    l.submitted_at,
    l.reviewed_by,
    l.reviewed_at,
    l.approved_by,
    l.approved_at,
    l.disbursed_by,
    l.disbursed_at
FROM loans l
JOIN users u ON l.user_id = u.id
ORDER BY l.submitted_at DESC;

-- Example: Query untuk melihat loans by status
SELECT 
    l.id,
    u.username,
    l.amount,
    l.status,
    l.submitted_at
FROM loans l
JOIN users u ON l.user_id = u.id
WHERE l.status = 'SUBMITTED'
ORDER BY l.submitted_at ASC;

-- Example: Query untuk melihat user dan role mereka
SELECT 
    u.id,
    u.username,
    u.email,
    GROUP_CONCAT(r.name) as roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.id, u.username, u.email;

-- Example: Query untuk statistics
SELECT 
    status,
    COUNT(*) as count,
    SUM(amount) as total_amount,
    AVG(amount) as avg_amount
FROM loans
GROUP BY status;
