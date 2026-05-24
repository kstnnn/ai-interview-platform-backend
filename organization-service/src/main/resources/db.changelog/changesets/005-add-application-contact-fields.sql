--liquibase formatted sql

--changeset kstnnn:005-add-application-contact-fields
--comment: Add explicit candidate contact fields to vacancy applications
ALTER TABLE vacancy_applications
    ADD COLUMN contact_email VARCHAR(320),
    ADD COLUMN contact_phone VARCHAR(255),
    ADD COLUMN contact_telegram VARCHAR(255),
    ADD COLUMN contact_linkedin VARCHAR(2048),
    ADD COLUMN contact_portfolio_url VARCHAR(2048),
    ADD COLUMN contact_hh_resume_url VARCHAR(2048);

--rollback ALTER TABLE vacancy_applications DROP COLUMN IF EXISTS contact_hh_resume_url;
--rollback ALTER TABLE vacancy_applications DROP COLUMN IF EXISTS contact_portfolio_url;
--rollback ALTER TABLE vacancy_applications DROP COLUMN IF EXISTS contact_linkedin;
--rollback ALTER TABLE vacancy_applications DROP COLUMN IF EXISTS contact_telegram;
--rollback ALTER TABLE vacancy_applications DROP COLUMN IF EXISTS contact_phone;
--rollback ALTER TABLE vacancy_applications DROP COLUMN IF EXISTS contact_email;
