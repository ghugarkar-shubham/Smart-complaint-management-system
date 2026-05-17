CREATE DATABASE IF NOT EXISTS complaint_system;
USE complaint_system;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE complaints (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(180) NOT NULL,
    category VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    resolved_by VARCHAR(120),
    CONSTRAINT fk_complaints_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

INSERT INTO users (id, full_name, email, password, role) VALUES
(1, 'System Admin', 'admin@complaints.com', '$2b$10$p5P5a/n8EsKAF8k2hYL.OOJcNxlqPMWGXVu5Ov6RZG5Jv7qMvpJSa', 'ADMIN'),
(2, 'Demo User', 'user@complaints.com', '$2a$10$VxJNv6aLZuZuHN8FP2hzre4jQFYEzM9DzT5gVfC2bMZ/5N/Xfx3jS', 'USER');

INSERT INTO complaints (title, category, description, status, user_id, resolved_by) VALUES
('Street light not working', 'Infrastructure', 'The street light near block A has been off for a week.', 'OPEN', 2, NULL),
('Water leakage in corridor', 'Facility', 'Persistent water leakage has caused slippery floors.', 'IN_PROGRESS', 2, 'Site Admin'),
('Delayed waste collection', 'Sanitation', 'Garbage collection is delayed every alternate day.', 'RESOLVED', 2, 'Municipal Team');
