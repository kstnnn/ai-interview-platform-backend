--liquibase formatted sql

--changeset kstnnn:004-add-application-candidate-snapshot
--comment: Add candidate snapshot fields to vacancy applications
ALTER TABLE vacancy_applications
    ADD COLUMN candidate_first_name VARCHAR(255),
    ADD COLUMN candidate_last_name VARCHAR(255),
    ADD COLUMN candidate_email VARCHAR(255);

--rollback ALTER TABLE vacancy_applications DROP COLUMN IF EXISTS candidate_email;
--rollback ALTER TABLE vacancy_applications DROP COLUMN IF EXISTS candidate_last_name;
--rollback ALTER TABLE vacancy_applications DROP COLUMN IF EXISTS candidate_first_name;
