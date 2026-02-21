--liquibase formatted sql

--changeset kstnnn:001-create-user-table
--comment: Create user table
CREATE TABLE users(
    id UUID PRIMARY KEY,
    provider_user_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    user_type VARCHAR(50) NOT NULL,
    user_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    email_verified BOOLEAN DEFAULT FALSE NOT NULL
);
--rollback DROP TABLE IF EXISTS users;