--liquibase formatted sql

--changeset kstnnn:006-add-interview-level-column-for-interview-sessions-table
--comment: Add new interview_level column for interview_sessions
ALTER TABLE interview_sessions ADD COLUMN interview_level VARCHAR(16);
UPDATE interview_sessions SET interview_level = 'MIDDLE' WHERE interview_level IS NULL;
ALTER TABLE interview_sessions ALTER COLUMN interview_level SET NOT NULL;
ALTER TABLE interview_sessions ADD CONSTRAINT ck_interview_sessions_interview_level 
	CHECK ( interview_level IN ('JUNIOR', 'MIDDLE', 'SENIOR') );
--rollback ALTER TABLE interview_sessions DROP CONSTRAINT IF EXISTS ck_interview_sessions_interview_level;
--rollback ALTER TABLE interview_sessions DROP COLUMN IF EXISTS interview_level;
