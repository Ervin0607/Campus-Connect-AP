DROP DATABASE IF EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

CREATE TABLE users (
                       UserID INT PRIMARY KEY,
                       UserName VARCHAR(100) NOT NULL UNIQUE,
                       Role VARCHAR(50) NOT NULL,
                       Password VARCHAR(100) NOT NULL,
                       Status VARCHAR(100) NOT NULL
);