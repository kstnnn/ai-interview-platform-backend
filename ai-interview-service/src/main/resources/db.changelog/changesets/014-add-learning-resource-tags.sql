--liquibase formatted sql

--changeset kstnnn:014-add-learning-resource-tags
--comment: Add tags and aliases for learning resources
CREATE TABLE learning_resource_tags (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL REFERENCES learning_resources(id) ON DELETE CASCADE,
    tag VARCHAR(120) NOT NULL,
    CONSTRAINT uq_learning_resource_tags UNIQUE (resource_id, tag)
);

CREATE INDEX idx_learning_resource_tags_tag ON learning_resource_tags(tag);
CREATE INDEX idx_learning_resource_tags_resource_id ON learning_resource_tags(resource_id);

INSERT INTO learning_resource_tags(resource_id, tag)
SELECT id, tag
FROM learning_resources lr
CROSS JOIN LATERAL regexp_split_to_table(
  CASE lr.topic
    WHEN 'java' THEN 'java,jvm,collections,oop,streams,concurrency,backend'
    WHEN 'spring' THEN 'spring,springboot,dependencyinjection,di,ioc,restapi,backend,java'
    WHEN 'sql' THEN 'sql,postgresql,database,queries,indexes,joins,transactions'
    WHEN 'postgresql' THEN 'postgresql,sql,database,indexes,transactions,joins'
    WHEN 'database' THEN 'database,sql,postgresql,schema,ddl,normalization,indexes'
    WHEN 'testing' THEN 'testing,unittest,integrationtest,junit,mockito,springboottest,tdd'
    WHEN 'architecture' THEN 'architecture,cleanarchitecture,hexagonal,layeredarchitecture,designpatterns,systemdesign'
    WHEN 'microservices' THEN 'microservices,distributedsystems,servicecommunication,resilience,patterns'
    WHEN 'docker' THEN 'docker,containers,containerization,devops,deployment'
    WHEN 'kubernetes' THEN 'kubernetes,k8s,containers,devops,deployment,orchestration'
    WHEN 'devops' THEN 'devops,docker,kubernetes,ci,cd,deployment,containers'
    WHEN 'javascript' THEN 'javascript,js,frontend,async,promises,dom'
    WHEN 'typescript' THEN 'typescript,ts,types,frontend,javascript'
    WHEN 'react' THEN 'react,frontend,components,hooks,state,jsx'
    WHEN 'node' THEN 'node,nodejs,javascript,backend,api,express'
    WHEN 'python' THEN 'python,backend,scripting,oop,async'
    WHEN 'django' THEN 'django,python,backend,web,orm'
    WHEN 'go' THEN 'go,golang,backend,concurrency,goroutines'
    WHEN 'csharp' THEN 'csharp,c#,dotnet,.net,backend,oop'
    WHEN 'dotnet' THEN 'dotnet,.net,csharp,c#,backend,aspnet'
    WHEN 'systemdesign' THEN 'systemdesign,architecture,scalability,databases,caching,loadbalancing'
    WHEN 'algorithms' THEN 'algorithms,datastructures,leetcode,complexity,big-o,interview'
    WHEN 'general' THEN 'general,interview,behavioral,star,communication'
    ELSE lr.topic
  END,
  ','
) AS tag;

--rollback DROP TABLE IF EXISTS learning_resource_tags;
