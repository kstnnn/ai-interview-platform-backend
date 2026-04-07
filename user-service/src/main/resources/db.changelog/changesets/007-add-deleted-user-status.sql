--liquibase formatted sql

--changeset kstnnn:007-add-deleted-user-status
--comment: Add DELETED status to user_status check constaint
ALTER TABLE users DROP CONSTRAINT IF EXISTS ck_users_user_status;
ALTER TABLE users ADD CONSTRAINT ck_users_user_status
	CHECK ( user_status IN ('ACTIVE', 'BLOCKED', 'DELETED') );
--rollback ALTER TABLE users DROP CONSTRAINT IF EXISTS ck_users_user_status;
--rollback ALTER TABLE users ADD CONSTRAINT ck_users_user_status CHECK ( user_status IN ('ACTIVE', 'BLOCKED') );

