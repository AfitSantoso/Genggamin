-- Create LoanDB database if not exists
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'LoanDB')
BEGIN
    CREATE DATABASE LoanDB;
    PRINT 'Database LoanDB created successfully';
END
ELSE
BEGIN
    PRINT 'Database LoanDB already exists';
END
GO

-- Create login 'bot' if not exists
IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'bot')
BEGIN
    CREATE LOGIN bot WITH PASSWORD = 'P@ssw0rd';
    PRINT 'Login bot created successfully';
END
ELSE
BEGIN
    PRINT 'Login bot already exists';
END
GO

USE LoanDB;
GO

-- Create user 'bot' in LoanDB if not exists
IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = 'bot')
BEGIN
    CREATE USER bot FOR LOGIN bot;
    PRINT 'User bot created successfully';
END
ELSE
BEGIN
    PRINT 'User bot already exists';
END
GO

-- Grant permissions to bot user
ALTER ROLE db_owner ADD MEMBER bot;
PRINT 'Permissions granted to bot user';
GO

PRINT 'Database initialization completed';
GO
