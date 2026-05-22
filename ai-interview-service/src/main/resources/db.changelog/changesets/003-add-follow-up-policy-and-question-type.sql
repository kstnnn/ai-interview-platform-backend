--liquibase formatted sql

--changeset kstnnn:003-add-follow-up-policy-and-question-type
--comment: Add follow-up limits and question type support
ALTER TABLE interview_sessions
    ADD COLUMN max_follow_ups_per_primary INTEGER;

UPDATE interview_sessions
SET max_follow_ups_per_primary = 1
WHERE max_follow_ups_per_primary IS NULL;

ALTER TABLE interview_sessions
    ALTER COLUMN max_follow_ups_per_primary SET NOT NULL;

ALTER TABLE interview_sessions
    ADD CONSTRAINT ck_interview_sessions_max_follow_ups_per_primary
        CHECK (max_follow_ups_per_primary > 0);

ALTER TABLE session_questions
    ADD COLUMN question_type VARCHAR(24);

UPDATE session_questions
SET question_type = 'PRIMARY'
WHERE question_type IS NULL;

ALTER TABLE session_questions
    ALTER COLUMN question_type SET NOT NULL;

ALTER TABLE session_questions
    ADD COLUMN parent_question_id UUID;

ALTER TABLE session_questions
    ADD CONSTRAINT fk_session_questions_parent_question
        FOREIGN KEY (parent_question_id) REFERENCES session_questions(id);

ALTER TABLE session_questions
    ADD CONSTRAINT ck_session_questions_question_type
        CHECK (question_type IN ('PRIMARY', 'FOLLOW_UP'));

ALTER TABLE session_questions
    ADD CONSTRAINT ck_session_questions_parent_relation
        CHECK (
            (question_type = 'PRIMARY' AND parent_question_id IS NULL)
            OR (question_type = 'FOLLOW_UP' AND parent_question_id IS NOT NULL)
        );

CREATE INDEX idx_session_questions_parent_question_id
    ON session_questions(parent_question_id);

--rollback DROP INDEX IF EXISTS idx_session_questions_parent_question_id;
--rollback ALTER TABLE session_questions DROP CONSTRAINT IF EXISTS ck_session_questions_parent_relation;
--rollback ALTER TABLE session_questions DROP CONSTRAINT IF EXISTS ck_session_questions_question_type;
--rollback ALTER TABLE session_questions DROP CONSTRAINT IF EXISTS fk_session_questions_parent_question;
--rollback ALTER TABLE session_questions DROP COLUMN IF EXISTS parent_question_id;
--rollback ALTER TABLE session_questions DROP COLUMN IF EXISTS question_type;
--rollback ALTER TABLE interview_sessions DROP CONSTRAINT IF EXISTS ck_interview_sessions_max_follow_ups_per_primary;
--rollback ALTER TABLE interview_sessions DROP COLUMN IF EXISTS max_follow_ups_per_primary;
