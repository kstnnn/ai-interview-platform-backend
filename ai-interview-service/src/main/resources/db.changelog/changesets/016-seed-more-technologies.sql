--liquibase formatted sql

--changeset kstnnn:016-seed-more-technologies
--comment: Seed additional technologies for question bank import
INSERT INTO technologies (tech_key, display_name, active) VALUES
  ('javascript', 'JavaScript', true),
  ('typescript', 'TypeScript', true),
  ('react', 'React', true),
  ('node', 'Node.js', true),
  ('go', 'Go', true),
  ('csharp', 'C#', true),
  ('dotnet', '.NET', true),
  ('rust', 'Rust', true)
ON CONFLICT (tech_key) DO UPDATE
SET display_name = EXCLUDED.display_name,
    active = EXCLUDED.active;

--rollback DELETE FROM technologies WHERE tech_key IN ('javascript', 'typescript', 'react', 'node', 'go', 'csharp', 'dotnet', 'rust');
