--liquibase formatted sql

--changeset kstnnn:002-add-min-questions-per-topic-and-selection-reason-check
--comment: Add minimum questions per topic and selection reason guardrail
ALTER TABLE interview_sessions
    ADD COLUMN min_questions_per_topic INTEGER;

UPDATE interview_sessions
SET min_questions_per_topic = 2
WHERE min_questions_per_topic IS NULL;

ALTER TABLE interview_sessions
    ALTER COLUMN min_questions_per_topic SET NOT NULL;

ALTER TABLE interview_sessions
    ADD CONSTRAINT ck_interview_sessions_min_questions_per_topic
        CHECK (min_questions_per_topic > 0);

ALTER TABLE session_questions
    ADD CONSTRAINT ck_session_questions_selection_reason
        CHECK (
            selection_reason IS NULL
            OR selection_reason IN (
                'BASELINE_COVERAGE',
                'WEAK_TOPIC_REINFORCEMENT',
                'DIFFICULTY_ESCALATION',
                'FOLLOW_UP_CLARIFICATION',
                'RANDOM_TIE_BREAK'
            )
        );

--rollback ALTER TABLE session_questions DROP CONSTRAINT IF EXISTS ck_session_questions_selection_reason;
--rollback ALTER TABLE interview_sessions DROP CONSTRAINT IF EXISTS ck_interview_sessions_min_questions_per_topic;
--rollback ALTER TABLE interview_sessions DROP COLUMN IF EXISTS min_questions_per_topic;
