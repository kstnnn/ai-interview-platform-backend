--liquibase formatted sql

--changeset kstnnn:015-add-more-learning-resources
--comment: Add more curated learning resources and roadmap aliases
INSERT INTO learning_resources(topic, title, url, type, language, difficulty)
SELECT topic, title, url, type, language, difficulty
FROM (VALUES
    ('security', 'Spring Security Reference', 'https://docs.spring.io/spring-security/reference/', 'DOC', 'en', 'ANY'),
    ('security', 'Baeldung Spring Security', 'https://www.baeldung.com/security-spring', 'ARTICLE', 'en', 'ANY'),
    ('security', 'OWASP Top 10', 'https://owasp.org/www-project-top-ten/', 'DOC', 'en', 'ANY'),
    ('security', 'JWT Introduction', 'https://jwt.io/introduction', 'DOC', 'en', 'ANY'),
    ('security', 'OAuth 2.0 Simplified', 'https://www.oauth.com/', 'DOC', 'en', 'ANY'),
    ('security', 'Spring Security на русском', 'https://habr.com/ru/articles/203318/', 'ARTICLE', 'ru', 'ANY'),
    ('database', 'Spring Data JPA Reference', 'https://docs.spring.io/spring-data/jpa/reference/', 'DOC', 'en', 'ANY'),
    ('database', 'Hibernate ORM User Guide', 'https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html', 'DOC', 'en', 'ANY'),
    ('database', 'Baeldung JPA Tutorials', 'https://www.baeldung.com/the-persistence-layer-with-spring-data-jpa', 'ARTICLE', 'en', 'ANY'),
    ('database', 'PostgreSQL Indexes', 'https://www.postgresql.org/docs/current/indexes.html', 'DOC', 'en', 'ANY'),
    ('database', 'SQLBolt Interactive SQL Lessons', 'https://sqlbolt.com/', 'PRACTICE', 'en', 'ANY'),
    ('database', 'Hibernate и JPA на русском', 'https://javarush.com/groups/posts/hibernate-java', 'ARTICLE', 'ru', 'ANY'),
    ('java', 'Oracle Java Tutorials', 'https://docs.oracle.com/javase/tutorial/', 'DOC', 'en', 'ANY'),
    ('java', 'Java Collections Framework', 'https://docs.oracle.com/javase/8/docs/technotes/guides/collections/overview.html', 'DOC', 'en', 'ANY'),
    ('java', 'Java Streams Guide', 'https://www.baeldung.com/java-8-streams', 'ARTICLE', 'en', 'ANY'),
    ('java', 'Java Exceptions', 'https://docs.oracle.com/javase/tutorial/essential/exceptions/', 'DOC', 'en', 'ANY'),
    ('java', 'JavaRush Java Syntax', 'https://javarush.com/quests/lectures/questsyntax', 'ARTICLE', 'ru', 'ANY'),
    ('spring', 'Spring Boot Reference', 'https://docs.spring.io/spring-boot/reference/', 'DOC', 'en', 'ANY'),
    ('spring', 'Spring Framework Reference', 'https://docs.spring.io/spring-framework/reference/', 'DOC', 'en', 'ANY'),
    ('spring', 'Spring REST Guide', 'https://spring.io/guides/gs/rest-service/', 'DOC', 'en', 'ANY'),
    ('testing', 'JUnit 5 User Guide', 'https://junit.org/junit5/docs/current/user-guide/', 'DOC', 'en', 'ANY'),
    ('testing', 'Mockito Documentation', 'https://site.mockito.org/', 'DOC', 'en', 'ANY'),
    ('testing', 'Testcontainers Guides', 'https://testcontainers.com/guides/', 'DOC', 'en', 'ANY'),
    ('architecture', 'Refactoring Guru Design Patterns', 'https://refactoring.guru/design-patterns', 'DOC', 'en', 'ANY'),
    ('architecture', 'Martin Fowler Architecture Guide', 'https://martinfowler.com/architecture/', 'ARTICLE', 'en', 'ANY'),
    ('microservices', 'Spring Cloud Patterns', 'https://spring.io/microservices', 'DOC', 'en', 'ANY'),
    ('javascript', 'MDN JavaScript First Steps', 'https://developer.mozilla.org/en-US/docs/Learn/JavaScript/First_steps', 'DOC', 'en', 'ANY'),
    ('javascript', 'JavaScript.info', 'https://javascript.info/', 'DOC', 'en', 'ANY'),
    ('typescript', 'TypeScript for JavaScript Programmers', 'https://www.typescriptlang.org/docs/handbook/typescript-in-5-minutes.html', 'DOC', 'en', 'ANY'),
    ('react', 'React Thinking in React', 'https://react.dev/learn/thinking-in-react', 'DOC', 'en', 'ANY'),
    ('node', 'Express Guide', 'https://expressjs.com/en/guide/routing.html', 'DOC', 'en', 'ANY'),
    ('python', 'Real Python Tutorials', 'https://realpython.com/', 'ARTICLE', 'en', 'ANY'),
    ('python', 'Python Data Structures', 'https://docs.python.org/3/tutorial/datastructures.html', 'DOC', 'en', 'ANY'),
    ('go', 'Effective Go', 'https://go.dev/doc/effective_go', 'DOC', 'en', 'ANY'),
    ('csharp', 'C# Fundamentals', 'https://learn.microsoft.com/en-us/dotnet/csharp/fundamentals/tutorials/oop', 'DOC', 'en', 'ANY'),
    ('dotnet', 'ASP.NET Core Fundamentals', 'https://learn.microsoft.com/en-us/aspnet/core/fundamentals/', 'DOC', 'en', 'ANY'),
    ('systemdesign', 'ByteByteGo System Design 101', 'https://github.com/ByteByteGoHq/system-design-101', 'DOC', 'en', 'ANY'),
    ('general', 'Pramp Interview Practice', 'https://www.pramp.com/', 'PRACTICE', 'en', 'ANY')
) AS resource(topic, title, url, type, language, difficulty)
WHERE NOT EXISTS (
    SELECT 1 FROM learning_resources existing WHERE existing.url = resource.url
);

INSERT INTO learning_resource_tags(resource_id, tag)
SELECT lr.id, tag
FROM learning_resources lr
CROSS JOIN LATERAL regexp_split_to_table(
  CASE lr.topic
    WHEN 'security' THEN 'security,auth,authentication,authorization,jwt,oauth2,springsecurity,owasp,csrf,cors'
    WHEN 'database' THEN 'dataaccess,data_access,database,sql,postgresql,jpa,hibernate,orm,repository,transactions,indexes,joins,persistence'
    WHEN 'java' THEN 'languagebasics,language_basics,java,jvm,oop,collections,streams,exceptions,generics,concurrency,backend'
    WHEN 'spring' THEN 'spring,springboot,dependencyinjection,di,ioc,restapi,backend,java,springmvc'
    WHEN 'testing' THEN 'testing,unittest,integrationtest,junit,mockito,testcontainers,springboottest,tdd'
    WHEN 'architecture' THEN 'architecture,cleanarchitecture,hexagonal,layeredarchitecture,designpatterns,systemdesign'
    WHEN 'microservices' THEN 'microservices,distributedsystems,servicecommunication,resilience,patterns,springcloud'
    WHEN 'javascript' THEN 'languagebasics,language_basics,javascript,js,frontend,async,promises,dom'
    WHEN 'typescript' THEN 'languagebasics,language_basics,typescript,ts,types,frontend,javascript'
    WHEN 'react' THEN 'react,frontend,components,hooks,state,jsx'
    WHEN 'node' THEN 'node,nodejs,javascript,backend,api,express'
    WHEN 'python' THEN 'languagebasics,language_basics,python,backend,scripting,oop,async'
    WHEN 'go' THEN 'languagebasics,language_basics,go,golang,backend,concurrency,goroutines'
    WHEN 'csharp' THEN 'languagebasics,language_basics,csharp,c#,dotnet,.net,backend,oop'
    WHEN 'dotnet' THEN 'dotnet,.net,csharp,c#,backend,aspnet'
    WHEN 'systemdesign' THEN 'systemdesign,architecture,scalability,databases,caching,loadbalancing'
    WHEN 'general' THEN 'general,interview,behavioral,star,communication,practice'
    ELSE lr.topic
  END,
  ','
) AS tag
ON CONFLICT (resource_id, tag) DO NOTHING;

--rollback DELETE FROM learning_resource_tags WHERE tag IN ('dataaccess', 'data_access', 'languagebasics', 'language_basics', 'springsecurity', 'oauth2', 'owasp', 'persistence');
