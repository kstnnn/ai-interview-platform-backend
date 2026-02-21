--liquibase formatted sql

--changeset kstnnn:003-add-users-check-constraints
--comment: Add check constraints for user_status and user_type
ALTER TABLE users ADD CONSTRAINT ck_users_user_status
    CHECK (user_status IN ('ACTIVE', 'BLOCKED'));
ALTER TABLE users ADD CONSTRAINT ck_users_user_type
    CHECK (user_type IN ('BUSINESS', 'PERSONAL'));
--rollback ALTER TABLE users DROP CONSTRAINT IF EXISTS ck_users_user_status;
--rollback ALTER TABLE users DROP CONSTRAINT IF EXISTS ck_users_user_type;