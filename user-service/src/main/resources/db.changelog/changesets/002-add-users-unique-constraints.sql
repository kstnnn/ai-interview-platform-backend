--liquibase formatted sql

--changeset kstnnn:002-add-users-unique-constraints
--comment: Add unique constraints for email and provider_user_id
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
ALTER TABLE users ADD CONSTRAINT uk_users_provider_user_id UNIQUE (provider_user_id);
--rollback ALTER TABLE users DROP CONSTRAINT IF EXISTS uk_users_email;
--rollback ALTER TABLE users DROP CONSTRAINT IF EXISTS uk_users_provider_user_id;