--liquibase formatted sql

--changeset kstnnn:004-seed-technologies.sql
--comment: Seed base technologies for interview sessions and question import
INSERT INTO technologies (tech_key, display_name, active) VALUES
  ('java', 'Java', true),
  ('spring', 'Spring', true),
  ('python', 'Python', true),
  ('django', 'Django', true),
  ('fastapi', 'FastAPI', true),
  ('postgresql', 'PostgreSQL', true),
  ('hibernate', 'Hibernate', true),
  ('kafka', 'Kafka', true),
  ('redis', 'Redis', true),
  ('system_design', 'System Design', true),
  ('testing', 'Testing', true),
  ('devops', 'DevOps', true)
ON CONFLICT (tech_key) DO UPDATE
SET display_name = EXCLUDED.display_name,
    active = EXCLUDED.active;
--rollback DELETE FROM technologies
--rollback WHERE tech_key IN (
--rollback   'java', 'spring', 'python', 'django', 'fastapi', 'postgresql',
--rollback   'hibernate', 'kafka', 'redis', 'system_design', 'testing', 'devops'
--rollback );

