--liquibase formatted sql

--changeset kstnnn:010-add-interview-language-column
--comment: Add interview_language column for interview_sessions
ALTER TABLE interview_sessions ADD COLUMN interview_language VARCHAR(32);
UPDATE interview_sessions SET interview_language = 'English' WHERE interview_language IS NULL;
ALTER TABLE interview_sessions ALTER COLUMN interview_language SET NOT NULL;
--rollback ALTER TABLE interview_sessions DROP COLUMN IF EXISTS interview_language;
