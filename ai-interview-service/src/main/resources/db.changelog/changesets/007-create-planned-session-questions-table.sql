--liquibase formatted sql

--changeset kstnnn:007-create-planned-session-questions-table.sql
--comment: Create planned_session_questions table
CREATE TABLE planned_session_questions (
	id UUID	PRIMARY KEY,
	session_id UUID NOT NULL,
	question_id UUID NOT NULL,
	planned_status VARCHAR(16)
);
ALTER TABLE planned_session_questions ADD CONSTRAINT ck_planned_session_questions_planned_status CHECK ( planned_status IN ('PLANNED', 'ASKED', 'CANCELED') );
--rollback ALTER TABLE planned_session_questions DROP CONSTRAINT IF EXISTS ck_planned_session_questions_planned_status;
--rollback DROP TABLE IF EXISTS planned_session_questions;
