--liquibase formatted sql

--changeset kstnnn:008-add-references-planned-session-questions-table.sql
--comment: Add references for session_id and question_id in planned_session_questions table
ALTER TABLE planned_session_questions
	ADD CONSTRAINT fk_planned_session_questions_interview_sessions
	FOREIGN KEY(session_id) REFERENCES interview_sessions(id);
ALTER TABLE planned_session_questions
	ADD CONSTRAINT fk_planned_session_questions_questions
	FOREIGN KEY(question_id) REFERENCES questions(id);
--rollback ALTER TABLE planned_session_questions DROP CONSTRAINT IF EXISTS fk_planned_session_questions_interview_sessions;
--rollback ALTER TABLE planned_session_questions DROP CONSTRAINT IF EXISTS fk_planned_session_questions_questions;
