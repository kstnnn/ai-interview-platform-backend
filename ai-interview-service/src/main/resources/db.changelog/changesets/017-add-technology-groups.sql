--liquibase formatted sql

--changeset kstnnn:017-add-technology-groups
--comment: Add grouping metadata for technology selectors
ALTER TABLE technologies
    ADD COLUMN group_key VARCHAR(64),
    ADD COLUMN group_name VARCHAR(128),
    ADD COLUMN sort_order INTEGER;

UPDATE technologies
SET group_key = CASE tech_key
        WHEN 'java' THEN 'languages'
        WHEN 'python' THEN 'languages'
        WHEN 'javascript' THEN 'languages'
        WHEN 'typescript' THEN 'languages'
        WHEN 'go' THEN 'languages'
        WHEN 'csharp' THEN 'languages'
        WHEN 'rust' THEN 'languages'
        WHEN 'spring' THEN 'frameworks'
        WHEN 'django' THEN 'frameworks'
        WHEN 'fastapi' THEN 'frameworks'
        WHEN 'react' THEN 'frameworks'
        WHEN 'dotnet' THEN 'frameworks'
        WHEN 'postgresql' THEN 'databases'
        WHEN 'hibernate' THEN 'databases'
        WHEN 'redis' THEN 'databases'
        WHEN 'kafka' THEN 'messaging'
        WHEN 'node' THEN 'runtime_platform'
        WHEN 'devops' THEN 'runtime_platform'
        WHEN 'system_design' THEN 'general'
        WHEN 'testing' THEN 'general'
        ELSE 'general'
    END,
    group_name = CASE tech_key
        WHEN 'java' THEN 'Programming Languages'
        WHEN 'python' THEN 'Programming Languages'
        WHEN 'javascript' THEN 'Programming Languages'
        WHEN 'typescript' THEN 'Programming Languages'
        WHEN 'go' THEN 'Programming Languages'
        WHEN 'csharp' THEN 'Programming Languages'
        WHEN 'rust' THEN 'Programming Languages'
        WHEN 'spring' THEN 'Frameworks'
        WHEN 'django' THEN 'Frameworks'
        WHEN 'fastapi' THEN 'Frameworks'
        WHEN 'react' THEN 'Frameworks'
        WHEN 'dotnet' THEN 'Frameworks'
        WHEN 'postgresql' THEN 'Databases & Persistence'
        WHEN 'hibernate' THEN 'Databases & Persistence'
        WHEN 'redis' THEN 'Databases & Persistence'
        WHEN 'kafka' THEN 'Messaging & Streaming'
        WHEN 'node' THEN 'Runtime & Platform'
        WHEN 'devops' THEN 'Runtime & Platform'
        WHEN 'system_design' THEN 'General'
        WHEN 'testing' THEN 'General'
        ELSE 'General'
    END,
    sort_order = CASE tech_key
        WHEN 'java' THEN 10
        WHEN 'python' THEN 20
        WHEN 'javascript' THEN 30
        WHEN 'typescript' THEN 40
        WHEN 'go' THEN 50
        WHEN 'csharp' THEN 60
        WHEN 'rust' THEN 70
        WHEN 'spring' THEN 110
        WHEN 'django' THEN 120
        WHEN 'fastapi' THEN 130
        WHEN 'react' THEN 140
        WHEN 'dotnet' THEN 150
        WHEN 'postgresql' THEN 210
        WHEN 'hibernate' THEN 220
        WHEN 'redis' THEN 230
        WHEN 'kafka' THEN 310
        WHEN 'node' THEN 410
        WHEN 'devops' THEN 420
        WHEN 'system_design' THEN 510
        WHEN 'testing' THEN 520
        ELSE 1000
    END;

ALTER TABLE technologies
    ALTER COLUMN group_key SET NOT NULL,
    ALTER COLUMN group_name SET NOT NULL,
    ALTER COLUMN sort_order SET NOT NULL;

--rollback ALTER TABLE technologies DROP COLUMN IF EXISTS sort_order;
--rollback ALTER TABLE technologies DROP COLUMN IF EXISTS group_name;
--rollback ALTER TABLE technologies DROP COLUMN IF EXISTS group_key;
