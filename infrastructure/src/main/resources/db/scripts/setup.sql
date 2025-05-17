-- Initialization script for Oracle Database in Docker

-- Create Maya user if it doesn't exist
DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count FROM dba_users WHERE username = 'MAYA';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'CREATE USER Maya IDENTIFIED BY Maya';
    EXECUTE IMMEDIATE 'GRANT CONNECT, RESOURCE, DBA TO Maya';
    EXECUTE IMMEDIATE 'ALTER USER Maya QUOTA UNLIMITED ON USERS';
  END IF;
END;
/

-- Connect as Maya user
CONNECT Maya/Maya@//localhost:1521/XE

-- Execute table creation scripts and stored procedures
@/opt/oracle/scripts/setup/create_plsql_packages.sql

-- Insert test data
@/opt/oracle/scripts/setup/insert_test_data.sql

COMMIT;

EXIT;