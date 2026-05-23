--liquibase formatted sql

--changeset kstnnn:001-create-organization-domain
--comment: Create organization and vacancy domain tables
CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL,
    name VARCHAR(160) NOT NULL,
    description TEXT,
    website_url VARCHAR(512),
    logo_url VARCHAR(512),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_organizations_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

CREATE TABLE organization_members (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_organization_members_user UNIQUE (organization_id, user_id),
    CONSTRAINT ck_organization_members_role CHECK (role IN ('OWNER', 'ADMIN', 'RECRUITER'))
);

CREATE TABLE vacancies (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    title VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    requirements TEXT,
    location VARCHAR(160),
    employment_type VARCHAR(32) NOT NULL,
    work_format VARCHAR(32) NOT NULL,
    level VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_by_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_vacancies_employment_type
        CHECK (employment_type IN ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP')),
    CONSTRAINT ck_vacancies_work_format CHECK (work_format IN ('REMOTE', 'HYBRID', 'ONSITE')),
    CONSTRAINT ck_vacancies_level CHECK (level IN ('JUNIOR', 'MIDDLE', 'SENIOR')),
    CONSTRAINT ck_vacancies_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'CLOSED', 'ARCHIVED'))
);

CREATE TABLE vacancy_technologies (
    id BIGSERIAL PRIMARY KEY,
    vacancy_id UUID NOT NULL REFERENCES vacancies(id) ON DELETE CASCADE,
    technology_key VARCHAR(64) NOT NULL,
    CONSTRAINT uq_vacancy_technologies UNIQUE (vacancy_id, technology_key)
);

CREATE INDEX idx_organizations_owner_user_id ON organizations(owner_user_id);
CREATE INDEX idx_organization_members_user_id ON organization_members(user_id);
CREATE INDEX idx_organization_members_organization_id ON organization_members(organization_id);
CREATE INDEX idx_vacancies_organization_id ON vacancies(organization_id);
CREATE INDEX idx_vacancies_status ON vacancies(status);
CREATE INDEX idx_vacancy_technologies_vacancy_id ON vacancy_technologies(vacancy_id);

--rollback DROP TABLE IF EXISTS vacancy_technologies;
--rollback DROP TABLE IF EXISTS vacancies;
--rollback DROP TABLE IF EXISTS organization_members;
--rollback DROP TABLE IF EXISTS organizations;
