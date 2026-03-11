--liquibase formatted sql

--changeset kstnnn:005-add-users-version-column
--comment: Add the version column in users table
ALTER TABLE users ADD COLUMN version BIGINT NOT NULL DEFAULT 0; 
--rollback ALTER TABLE users DROP COLUMN IF EXISTS version;
