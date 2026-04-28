--liquibase formatted sql

--changeset kstnnn:005-add-new-columns-for-questions-table.sql
--comment: Add new columns for questions table
ALTER TABLE questions ADD COLUMN external_id VARCHAR(64);
ALTER TABLE questions ALTER COLUMN external_id SET NOT NULL;
ALTER TABLE questions ADD CONSTRAINT uq_questions_external_id UNIQUE(external_id);
ALTER TABLE questions ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;
--rollback ALTER TABLE questions DROP CONSTRAINT IF EXISTS uq_questions_external_id;
--rollback ALTER TABLE questions DROP COLUMN IF EXISTS external_id;
--rollback ALTER TABLE questions DROP COLUMN IF EXISTS updated_at;
