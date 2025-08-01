-- Initialize databases for all microservices based on application.properties
CREATE DATABASE IF NOT EXISTS lenses;           -- lenses-service
CREATE DATABASE IF NOT EXISTS frames;           -- frames-service  
CREATE DATABASE IF NOT EXISTS glass_data;       -- glass-service
CREATE DATABASE IF NOT EXISTS sunglass_db;      -- sunglasses-service
CREATE DATABASE IF NOT EXISTS cartdb;           -- cart-service
CREATE DATABASE IF NOT EXISTS orderdb;          -- finalorder-service
CREATE DATABASE IF NOT EXISTS lenscart;         -- customer-admin-service
-- Note: product-service has no database configuration

-- Grant privileges to root user for all databases
GRANT ALL PRIVILEGES ON lenses.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON frames.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON glass_data.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON sunglass_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON cartdb.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON orderdb.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON lenscart.* TO 'root'@'%';

FLUSH PRIVILEGES;