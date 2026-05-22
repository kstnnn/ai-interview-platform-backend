--liquibase formatted sql

--changeset kstnnn:009-alter-knowledge-gaps-json-to-text
--comment: Change knowledge_gaps_json column type from jsonb to text
ALTER TABLE answer_evaluations ALTER COLUMN knowledge_gaps_json TYPE text;
--rollback ALTER TABLE answer_evaluations ALTER COLUMN knowledge_gaps_json TYPE jsonb;
