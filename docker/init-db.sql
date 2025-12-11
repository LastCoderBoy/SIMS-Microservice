-- ========================================
-- SIMS Database Initialization Script
-- ========================================

-- Create auth database
CREATE DATABASE IF NOT EXISTS sims_auth_db;

-- Create core database
CREATE DATABASE IF NOT EXISTS sims_core_db;

-- Grant ALL privileges to JK_Credentials user
GRANT ALL PRIVILEGES ON sims_auth_db.* TO 'JK_Credentials'@'%';
GRANT ALL PRIVILEGES ON sims_core_db.* TO 'JK_Credentials'@'%';

-- Flush privileges to apply changes
FLUSH PRIVILEGES;

-- Verify databases created
SHOW DATABASES;