CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150),
    phone VARCHAR(20),
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    updated_at DATETIME2
);

CREATE TABLE roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE password_reset_tokens (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expired_at DATETIME2 NOT NULL,
    used BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id)
);

--B CUSTOMERS
CREATE TABLE customers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    nik VARCHAR(20) NOT NULL UNIQUE,
    address VARCHAR(255),
    date_of_birth DATE,
    monthly_income DECIMAL(18,2),
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT fk_customer_user FOREIGN KEY (user_id) REFERENCES users(id)
);

--C PLAFOND
CREATE TABLE plafonds (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    min_income DECIMAL(18,2) NOT NULL,
    max_amount DECIMAL(18,2) NOT NULL,
    tenor_month BIGINT NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    is_active BIT DEFAULT 1
);

CREATE TABLE loans (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    plafond_id BIGINT,
    loan_amount DECIMAL(18,2) NOT NULL,
    tenor_month BIGINT NOT NULL,
    interest_rate DECIMAL(5,2),
    status VARCHAR(30) NOT NULL,
    submission_date DATETIME2 DEFAULT SYSDATETIME(),
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    updated_at DATETIME2,
    CONSTRAINT fk_loan_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_loan_plafond FOREIGN KEY (plafond_id) REFERENCES plafonds(id)
);

-- REVIEW – APPROVAL – DISBURSEMENT
CREATE TABLE loan_reviews (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    loan_id BIGINT NOT NULL UNIQUE,
    reviewed_by BIGINT NOT NULL,
    review_notes VARCHAR(500),
    review_status VARCHAR(30) NOT NULL,
    reviewed_at DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT fk_review_loan FOREIGN KEY (loan_id) REFERENCES loans(id),
    CONSTRAINT fk_review_user FOREIGN KEY (reviewed_by) REFERENCES users(id)
);

CREATE TABLE loan_approvals (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    loan_id BIGINT NOT NULL UNIQUE,
    approved_by BIGINT NOT NULL,
    approval_status VARCHAR(30) NOT NULL,
    approval_notes VARCHAR(500),
    approved_at DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT fk_approval_loan FOREIGN KEY (loan_id) REFERENCES loans(id),
    CONSTRAINT fk_approval_user FOREIGN KEY (approved_by) REFERENCES users(id)
);

CREATE TABLE loan_disbursements (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    loan_id BIGINT NOT NULL UNIQUE,
    disbursed_by BIGINT NOT NULL,
    disbursement_amount DECIMAL(18,2) NOT NULL,
    disbursement_date DATETIME2 DEFAULT SYSDATETIME(),
    bank_account VARCHAR(100),
    status VARCHAR(30),
    CONSTRAINT fk_disbursement_loan FOREIGN KEY (loan_id) REFERENCES loans(id),
    CONSTRAINT fk_disbursement_user FOREIGN KEY (disbursed_by) REFERENCES users(id)
);

--NOTIF
CREATE TABLE notifications (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    loan_id BIGINT,
    type VARCHAR(50),
    channel VARCHAR(20),
    title VARCHAR(150),
    message VARCHAR(500),
    is_read BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_notif_loan FOREIGN KEY (loan_id) REFERENCES loans(id)
);

--doc api
CREATE TABLE api_audit_logs (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT,
    endpoint VARCHAR(255),
    http_method VARCHAR(10),
    request_time DATETIME2 DEFAULT SYSDATETIME(),
    status_code BIGINT,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id)
);



-- isi adata ==================
INSERT INTO roles (name, description) VALUES
('CUSTOMER', 'Customer aplikasi'),
('MARKETING', 'Marketing'),
('BRANCH_MANAGER', 'Branch Manager'),
('BACK_OFFICE', 'Back Office');

INSERT INTO users (
    username, email, password_hash, full_name, phone, is_active
) VALUES (
    'ustafit',
    'afit@gmail.com',
    'afit123',
    'Afit kece',
    '08123456789',
    1
);

EXEC sp_help 'plafonds';


SELECT 
    fk.name AS fk_name,
    OBJECT_NAME(fk.parent_object_id) AS table_name
FROM sys.foreign_keys fk
WHERE fk.referenced_object_id = OBJECT_ID('users');

SELECT 
    fk.name AS fk_name,
    OBJECT_NAME(fk.parent_object_id) AS table_name
FROM sys.foreign_keys fk
WHERE fk.referenced_object_id = OBJECT_ID('customers');


select * from users









