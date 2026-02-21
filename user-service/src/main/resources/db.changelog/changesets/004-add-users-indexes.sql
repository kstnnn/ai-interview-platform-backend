--liquibase formatted sql

--changeset kstnnn:004-add-users-indexes
--comment: Add indexes for email and provider_user_id
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider_user_id ON users(provider_user_id);
--rollback DROP INDEX IF EXISTS idx_users_email;
--rollback DROP INDEX IF EXISTS idx_users_provider_user_id;