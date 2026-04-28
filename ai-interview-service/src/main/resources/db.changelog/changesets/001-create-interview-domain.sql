--liquibase formatted sql

--changeset kstnnn:001-create-interview-domain
--comment: Create interview session domain tables
CREATE TABLE interview_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    min_questions INTEGER NOT NULL,
    max_questions INTEGER NOT NULL,
    target_confidence NUMERIC(4,3) NOT NULL,
    status VARCHAR(32) NOT NULL,
    finished_reason VARCHAR(48),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    CONSTRAINT ck_interview_sessions_status
        CHECK (status IN ('CREATED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_interview_sessions_finished_reason
        CHECK (
            finished_reason IS NULL
            OR finished_reason IN ('CONFIDENCE_REACHED', 'COVERAGE_COMPLETED', 'MAX_QUESTIONS_REACHED', 'MANUAL_STOP')
        ),
    CONSTRAINT ck_interview_sessions_questions
        CHECK (min_questions > 0 AND max_questions > 0 AND min_questions <= max_questions),
    CONSTRAINT ck_interview_sessions_target_confidence
        CHECK (target_confidence >= 0 AND target_confidence <= 1)
);

CREATE TABLE technologies (
    id BIGSERIAL PRIMARY KEY,
    tech_key VARCHAR(64) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_technologies_key UNIQUE (tech_key)
);

CREATE TABLE interview_session_technologies (
    id BIGSERIAL PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES interview_sessions(id) ON DELETE CASCADE,
    technology_id BIGINT NOT NULL REFERENCES technologies(id),
    CONSTRAINT uq_interview_session_technologies UNIQUE (session_id, technology_id)
);

CREATE TABLE questions (
    id UUID PRIMARY KEY,
    technology_id BIGINT NOT NULL REFERENCES technologies(id),
    topic VARCHAR(80) NOT NULL,
    subtopic VARCHAR(80),
    difficulty VARCHAR(24) NOT NULL,
    question_text TEXT NOT NULL,
    expected_answer TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_questions_difficulty
        CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD'))
);

CREATE TABLE session_questions (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES interview_sessions(id) ON DELETE CASCADE,
    question_id UUID REFERENCES questions(id) ON DELETE SET NULL,
    round_number INTEGER NOT NULL,
    topic VARCHAR(80) NOT NULL,
    subtopic VARCHAR(80),
    difficulty VARCHAR(24) NOT NULL,
    question_text_snapshot TEXT NOT NULL,
    selection_reason VARCHAR(64),
    asked_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_session_questions_round
        CHECK (round_number > 0),
    CONSTRAINT ck_session_questions_difficulty
        CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    CONSTRAINT uq_session_questions_round UNIQUE (session_id, round_number)
);

CREATE TABLE session_answers (
    id UUID PRIMARY KEY,
    session_question_id UUID NOT NULL REFERENCES session_questions(id) ON DELETE CASCADE,
    answer_text TEXT NOT NULL,
    answered_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    duration_sec INTEGER,
    CONSTRAINT uq_session_answers_question UNIQUE (session_question_id),
    CONSTRAINT ck_session_answers_duration CHECK (duration_sec IS NULL OR duration_sec >= 0)
);

CREATE TABLE answer_evaluations (
    id UUID PRIMARY KEY,
    session_answer_id UUID NOT NULL REFERENCES session_answers(id) ON DELETE CASCADE,
    correctness_score NUMERIC(3,2) NOT NULL,
    depth_score NUMERIC(3,2) NOT NULL,
    practical_score NUMERIC(3,2) NOT NULL,
    total_score NUMERIC(3,2) NOT NULL,
    feedback TEXT,
    knowledge_gaps_json JSONB,
    evaluated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_answer_evaluations_answer UNIQUE (session_answer_id),
    CONSTRAINT ck_answer_evaluations_correctness CHECK (correctness_score >= 0 AND correctness_score <= 1),
    CONSTRAINT ck_answer_evaluations_depth CHECK (depth_score >= 0 AND depth_score <= 1),
    CONSTRAINT ck_answer_evaluations_practical CHECK (practical_score >= 0 AND practical_score <= 1),
    CONSTRAINT ck_answer_evaluations_total CHECK (total_score >= 0 AND total_score <= 1)
);

CREATE TABLE session_topic_states (
    id BIGSERIAL PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES interview_sessions(id) ON DELETE CASCADE,
    topic VARCHAR(80) NOT NULL,
    questions_asked INTEGER NOT NULL DEFAULT 0,
    avg_score NUMERIC(3,2) NOT NULL DEFAULT 0,
    mastery_score NUMERIC(3,2) NOT NULL DEFAULT 0,
    confidence_score NUMERIC(3,2) NOT NULL DEFAULT 0,
    last_asked_round INTEGER,
    CONSTRAINT uq_session_topic_states UNIQUE (session_id, topic),
    CONSTRAINT ck_session_topic_states_questions_asked CHECK (questions_asked >= 0),
    CONSTRAINT ck_session_topic_states_avg CHECK (avg_score >= 0 AND avg_score <= 1),
    CONSTRAINT ck_session_topic_states_mastery CHECK (mastery_score >= 0 AND mastery_score <= 1),
    CONSTRAINT ck_session_topic_states_confidence CHECK (confidence_score >= 0 AND confidence_score <= 1)
);

CREATE INDEX idx_questions_technology_topic
    ON questions(technology_id, topic);

CREATE INDEX idx_questions_topic_difficulty
    ON questions(topic, difficulty);

CREATE INDEX idx_session_questions_session_id
    ON session_questions(session_id);

CREATE INDEX idx_session_questions_question_id
    ON session_questions(question_id);

CREATE INDEX idx_session_topic_states_session_id
    ON session_topic_states(session_id);

--rollback DROP TABLE IF EXISTS session_topic_states;
--rollback DROP TABLE IF EXISTS answer_evaluations;
--rollback DROP TABLE IF EXISTS session_answers;
--rollback DROP TABLE IF EXISTS session_questions;
--rollback DROP TABLE IF EXISTS questions;
--rollback DROP TABLE IF EXISTS interview_session_technologies;
--rollback DROP TABLE IF EXISTS technologies;
--rollback DROP TABLE IF EXISTS interview_sessions;
