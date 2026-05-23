--liquibase formatted sql

--changeset kstnnn:002-add-vacancy-questions-and-applications
--comment: Add vacancy custom questions and candidate applications
CREATE TABLE vacancy_questions (
    id UUID PRIMARY KEY,
    vacancy_id UUID NOT NULL REFERENCES vacancies(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    expected_answer TEXT,
    evaluation_rubric TEXT,
    topic VARCHAR(80),
    required BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE vacancy_applications (
    id UUID PRIMARY KEY,
    vacancy_id UUID NOT NULL REFERENCES vacancies(id) ON DELETE CASCADE,
    candidate_user_id UUID NOT NULL,
    interview_session_id UUID,
    status VARCHAR(40) NOT NULL,
    cover_letter TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_vacancy_applications_candidate UNIQUE (vacancy_id, candidate_user_id),
    CONSTRAINT ck_vacancy_applications_status
        CHECK (status IN ('INTERVIEW_CREATED', 'INTERVIEW_IN_PROGRESS', 'INTERVIEW_COMPLETED', 'REJECTED', 'WITHDRAWN'))
);

CREATE INDEX idx_vacancy_questions_vacancy_id ON vacancy_questions(vacancy_id);
CREATE INDEX idx_vacancy_applications_vacancy_id ON vacancy_applications(vacancy_id);
CREATE INDEX idx_vacancy_applications_candidate_user_id ON vacancy_applications(candidate_user_id);
CREATE INDEX idx_vacancy_applications_interview_session_id ON vacancy_applications(interview_session_id);

--rollback DROP TABLE IF EXISTS vacancy_applications;
--rollback DROP TABLE IF EXISTS vacancy_questions;
