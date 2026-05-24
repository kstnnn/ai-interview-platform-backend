--liquibase formatted sql

--changeset kstnnn:013-add-session-type-and-learning-resources
--comment: Add interview session type and curated learning resources
ALTER TABLE interview_sessions
    ADD COLUMN session_type VARCHAR(32) NOT NULL DEFAULT 'MOCK';

UPDATE interview_sessions
SET session_type = 'VACANCY_APPLICATION'
WHERE application_id IS NOT NULL;

ALTER TABLE interview_sessions
    ADD CONSTRAINT ck_interview_sessions_session_type
        CHECK (session_type IN ('MOCK', 'VACANCY_APPLICATION'));

CREATE TABLE learning_resources (
    id BIGSERIAL PRIMARY KEY,
    topic VARCHAR(80) NOT NULL,
    title VARCHAR(240) NOT NULL,
    url VARCHAR(1024) NOT NULL,
    type VARCHAR(24) NOT NULL,
    language VARCHAR(8) NOT NULL,
    difficulty VARCHAR(24) NOT NULL DEFAULT 'ANY',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_learning_resources_type CHECK (type IN ('ARTICLE', 'DOC', 'VIDEO', 'PRACTICE'))
);

CREATE INDEX idx_learning_resources_topic ON learning_resources(topic);

INSERT INTO learning_resources(topic, title, url, type, language, difficulty) VALUES
('java', 'Java Tutorials', 'https://dev.java/learn/', 'DOC', 'en', 'ANY'),
('java', 'Baeldung Java Tutorials', 'https://www.baeldung.com/java-tutorial', 'ARTICLE', 'en', 'ANY'),
('spring', 'Spring Guides', 'https://spring.io/guides', 'DOC', 'en', 'ANY'),
('spring', 'Baeldung Spring Tutorials', 'https://www.baeldung.com/spring-tutorial', 'ARTICLE', 'en', 'ANY'),
('sql', 'PostgreSQL Tutorial', 'https://www.postgresql.org/docs/current/tutorial.html', 'DOC', 'en', 'ANY'),
('postgresql', 'PostgreSQL Tutorial', 'https://www.postgresql.org/docs/current/tutorial.html', 'DOC', 'en', 'ANY'),
('database', 'Database Design Basics', 'https://www.postgresql.org/docs/current/ddl.html', 'DOC', 'en', 'ANY'),
('testing', 'Spring Boot Testing', 'https://www.baeldung.com/spring-boot-testing', 'ARTICLE', 'en', 'ANY'),
('architecture', 'Clean Architecture', 'https://8thlight.com/insights/a-color-coded-guide-to-ports-and-adapters', 'ARTICLE', 'en', 'ANY'),
('microservices', 'Microservices.io Patterns', 'https://microservices.io/patterns/', 'DOC', 'en', 'ANY'),
('docker', 'Docker Get Started', 'https://docs.docker.com/get-started/', 'DOC', 'en', 'ANY'),
('kubernetes', 'Kubernetes Basics', 'https://kubernetes.io/docs/tutorials/kubernetes-basics/', 'DOC', 'en', 'ANY'),
('devops', 'Docker Get Started', 'https://docs.docker.com/get-started/', 'DOC', 'en', 'ANY'),
('javascript', 'MDN JavaScript Guide', 'https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide', 'DOC', 'en', 'ANY'),
('typescript', 'TypeScript Handbook', 'https://www.typescriptlang.org/docs/handbook/intro.html', 'DOC', 'en', 'ANY'),
('react', 'React Learn', 'https://react.dev/learn', 'DOC', 'en', 'ANY'),
('node', 'Node.js Learn', 'https://nodejs.org/en/learn', 'DOC', 'en', 'ANY'),
('python', 'Python Tutorial', 'https://docs.python.org/3/tutorial/', 'DOC', 'en', 'ANY'),
('django', 'Django Getting Started', 'https://docs.djangoproject.com/en/stable/intro/', 'DOC', 'en', 'ANY'),
('go', 'A Tour of Go', 'https://go.dev/tour/', 'DOC', 'en', 'ANY'),
('csharp', 'Microsoft C# Documentation', 'https://learn.microsoft.com/en-us/dotnet/csharp/', 'DOC', 'en', 'ANY'),
('dotnet', '.NET Documentation', 'https://learn.microsoft.com/en-us/dotnet/', 'DOC', 'en', 'ANY'),
('systemdesign', 'System Design Primer', 'https://github.com/donnemartin/system-design-primer', 'DOC', 'en', 'ANY'),
('algorithms', 'NeetCode Practice', 'https://neetcode.io/practice', 'PRACTICE', 'en', 'ANY'),
('general', 'STAR Interview Method', 'https://www.themuse.com/advice/star-interview-method', 'ARTICLE', 'en', 'ANY'),
('java', 'Java: руководство для начинающих', 'https://metanit.com/java/tutorial/', 'DOC', 'ru', 'ANY'),
('javascript', 'JavaScript: учебник', 'https://learn.javascript.ru/', 'DOC', 'ru', 'ANY'),
('sql', 'SQL учебник', 'https://metanit.com/sql/tutorial/', 'DOC', 'ru', 'ANY'),
('python', 'Python учебник', 'https://metanit.com/python/tutorial/', 'DOC', 'ru', 'ANY');

--rollback DROP TABLE IF EXISTS learning_resources;
--rollback ALTER TABLE interview_sessions DROP CONSTRAINT IF EXISTS ck_interview_sessions_session_type;
--rollback ALTER TABLE interview_sessions DROP COLUMN IF EXISTS session_type;
