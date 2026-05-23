--liquibase formatted sql

--changeset kstnnn:003-add-vacancy-interview-settings
--comment: Add interview primary question and follow-up settings to vacancies
ALTER TABLE vacancies
    ADD COLUMN min_primary_questions INTEGER NOT NULL DEFAULT 5,
    ADD COLUMN max_primary_questions INTEGER NOT NULL DEFAULT 8,
    ADD COLUMN max_follow_ups_per_primary INTEGER NOT NULL DEFAULT 1;

ALTER TABLE vacancies
    ADD CONSTRAINT ck_vacancies_primary_question_settings
        CHECK (
            min_primary_questions >= 1
            AND max_primary_questions >= min_primary_questions
            AND max_primary_questions <= 30
        ),
    ADD CONSTRAINT ck_vacancies_max_follow_ups_per_primary
        CHECK (max_follow_ups_per_primary >= 0 AND max_follow_ups_per_primary <= 2);

--rollback ALTER TABLE vacancies DROP CONSTRAINT IF EXISTS ck_vacancies_max_follow_ups_per_primary;
--rollback ALTER TABLE vacancies DROP CONSTRAINT IF EXISTS ck_vacancies_primary_question_settings;
--rollback ALTER TABLE vacancies DROP COLUMN IF EXISTS max_follow_ups_per_primary;
--rollback ALTER TABLE vacancies DROP COLUMN IF EXISTS max_primary_questions;
--rollback ALTER TABLE vacancies DROP COLUMN IF EXISTS min_primary_questions;
