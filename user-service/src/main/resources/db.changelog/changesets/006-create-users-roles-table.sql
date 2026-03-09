--liquibase formatted sql

--changeset kstnnn:006-create-users-roles-table
--comment: Create users roles table
CREATE TABLE users_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);
--rollback DROP TABLE users_roles
