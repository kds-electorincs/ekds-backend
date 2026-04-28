-- KDS database setup
-- Idempotent: safe to run multiple times.

-- Create database if it doesn't exist
SELECT 'CREATE DATABASE kds_dev'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'kds_dev')\gexec

-- Create user if it doesn't exist; update password either way
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'kds_user') THEN
        CREATE USER kds_user WITH PASSWORD 'kds_dev_password';
        RAISE NOTICE 'Created user kds_user';
ELSE
        ALTER USER kds_user WITH PASSWORD 'kds_dev_password';
        RAISE NOTICE 'Updated password for existing user kds_user';
END IF;
END
$$;

-- Grant database-level privileges
GRANT ALL PRIVILEGES ON DATABASE kds_dev TO kds_user;
ALTER DATABASE kds_dev OWNER TO kds_user;