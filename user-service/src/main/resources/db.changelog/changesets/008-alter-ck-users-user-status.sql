--liquibase formatted sql

--changeset kstnnn:008-alter-ck-users-user-status.sql
--comment: Change check constraint for user_status row from users table
ALTER TABLE users 
	DROP CONSTRAINT ck_users_user_status,
	ADD CONSTRAINT ck_users_user_status 
		CHECK (user_status IN ('ACTIVE', 'BLOCKED', 'PENDING_ONBOARDING', 'DELETED'));
--rollback ALTER TABLE users DROP CONSTRAINT IF EXISTS ck_users_user_status ADD CONSTRAINT ck_users_user_status CHECK (user_status IN ('ACTIVE', 'BLOCKED'));
