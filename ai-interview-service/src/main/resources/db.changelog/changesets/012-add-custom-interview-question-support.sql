--liquibase formatted sql

--changeset kstnnn:012-add-custom-interview-question-support
--comment: Add vacancy application and custom question snapshots to interview sessions
ALTER TABLE interview_sessions
    ADD COLUMN vacancy_id UUID,
    ADD COLUMN application_id UUID;

ALTER TABLE planned_session_questions
    ALTER COLUMN question_id DROP NOT NULL,
    ADD COLUMN question_text_snapshot TEXT,
    ADD COLUMN expected_answer_snapshot TEXT,
    ADD COLUMN evaluation_rubric TEXT,
    ADD COLUMN topic VARCHAR(80),
    ADD COLUMN subtopic VARCHAR(80),
    ADD COLUMN difficulty VARCHAR(24),
    ADD COLUMN source_type VARCHAR(32) NOT NULL DEFAULT 'QUESTION_BANK',
    ADD COLUMN external_question_id UUID,
    ADD COLUMN display_order INTEGER NOT NULL DEFAULT 0;

ALTER TABLE session_questions
    ADD COLUMN expected_answer_snapshot TEXT,
    ADD COLUMN source_type VARCHAR(32) NOT NULL DEFAULT 'QUESTION_BANK',
    ADD COLUMN external_question_id UUID;

ALTER TABLE planned_session_questions
    ADD CONSTRAINT ck_planned_session_questions_source_type
        CHECK (source_type IN ('QUESTION_BANK', 'VACANCY_CUSTOM', 'AI_FOLLOW_UP'));

ALTER TABLE session_questions
    ADD CONSTRAINT ck_session_questions_source_type
        CHECK (source_type IN ('QUESTION_BANK', 'VACANCY_CUSTOM', 'AI_FOLLOW_UP'));

UPDATE planned_session_questions psq
SET question_text_snapshot = q.question_text,
    expected_answer_snapshot = q.expected_answer,
    topic = q.topic,
    subtopic = q.subtopic,
    difficulty = q.difficulty,
    display_order = 1000
FROM questions q
WHERE psq.question_id = q.id;

--rollback ALTER TABLE session_questions DROP CONSTRAINT IF EXISTS ck_session_questions_source_type;
--rollback ALTER TABLE planned_session_questions DROP CONSTRAINT IF EXISTS ck_planned_session_questions_source_type;
--rollback ALTER TABLE session_questions DROP COLUMN IF EXISTS external_question_id;
--rollback ALTER TABLE session_questions DROP COLUMN IF EXISTS source_type;
--rollback ALTER TABLE session_questions DROP COLUMN IF EXISTS expected_answer_snapshot;
--rollback ALTER TABLE planned_session_questions DROP COLUMN IF EXISTS display_order;
--rollback ALTER TABLE planned_session_questions DROP COLUMN IF EXISTS external_question_id;
--rollback ALTER TABLE planned_session_questions DROP COLUMN IF EXISTS source_type;
--rollback ALTER TABLE planned_session_questions DROP COLUMN IF EXISTS difficulty;
--rollback ALTER TABLE planned_session_questions DROP COLUMN IF EXISTS subtopic;
--rollback ALTER TABLE planned_session_questions DROP COLUMN IF EXISTS topic;
--rollback ALTER TABLE planned_session_questions DROP COLUMN IF EXISTS evaluation_rubric;
--rollback ALTER TABLE planned_session_questions DROP COLUMN IF EXISTS expected_answer_snapshot;
--rollback ALTER TABLE planned_session_questions DROP COLUMN IF EXISTS question_text_snapshot;
--rollback ALTER TABLE planned_session_questions ALTER COLUMN question_id SET NOT NULL;
--rollback ALTER TABLE interview_sessions DROP COLUMN IF EXISTS application_id;
--rollback ALTER TABLE interview_sessions DROP COLUMN IF EXISTS vacancy_id;
