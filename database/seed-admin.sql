-- Seed Script for Admin User
-- Password: admin@123
-- Bcrypt Hash: $2b$10$p5P5a/n8EsKAF8k2hYL.OOJcNxlqPMWGXVu5Ov6RZG5Jv7qMvpJSa

USE complaint_system;

-- Delete existing admin if exists
DELETE FROM users WHERE email = 'admin@complaints.com';

-- Insert new admin user with password: admin@123
INSERT INTO users (id, full_name, email, password, role, created_at) VALUES
(1, 'System Admin', 'admin@complaints.com', '$2b$10$p5P5a/n8EsKAF8k2hYL.OOJcNxlqPMWGXVu5Ov6RZG5Jv7qMvpJSa', 'ADMIN', NOW());

-- Verify insertion
SELECT id, full_name, email, role FROM users WHERE email = 'admin@complaints.com';
