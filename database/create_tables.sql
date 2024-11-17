CREATE DATABASE customerms;

USE customerms;

CREATE TABLE customer (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    dni VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL
);

CREATE TABLE bankAccount (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    balance DOUBLE DEFAULT 0.0,
    account_type ENUM('SAVINGS', 'CHECKING') NOT NULL, -- Enum for account type
    customer_id INT NOT NULL, -- Foreign key to Customer
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
);
