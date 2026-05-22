--liquibase formatted sql

--changeset kstnnn:011-set-default-interview-language-to-russian
--comment: Set default interview language to Russian
ALTER TABLE interview_sessions ALTER COLUMN interview_language SET DEFAULT 'Russian';
--rollback ALTER TABLE interview_sessions ALTER COLUMN interview_language DROP DEFAULT;
