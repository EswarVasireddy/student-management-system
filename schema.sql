-- Student Management System schema
-- MySQL / MariaDB

CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    major VARCHAR(100) NOT NULL,
    gpa DECIMAL(3,2) NOT NULL CHECK (gpa BETWEEN 0.0 AND 4.0),
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_major (major)
);

INSERT INTO students (name, email, major, gpa) VALUES
('Asha Patel', 'asha.patel@example.com', 'Computer Science', 3.80),
('Rahul Mehta', 'rahul.mehta@example.com', 'Computer Science', 3.20),
('Divya Nair', 'divya.nair@example.com', 'Electronics', 3.95),
('Farhan Ali', 'farhan.ali@example.com', 'Mechanical', 2.90),
('Meera Iyer', 'meera.iyer@example.com', 'Computer Science', 3.55);
