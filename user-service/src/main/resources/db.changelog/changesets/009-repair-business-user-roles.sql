--liquibase formatted sql

--changeset kstnnn:009-repair-business-user-roles
--comment: Repair existing business users that were onboarded with candidate role
INSERT INTO users_roles (user_id, role)
SELECT u.id, 'MANAGER'
FROM users u
WHERE u.user_type = 'BUSINESS'
  AND u.user_status <> 'DELETED'
  AND NOT EXISTS (
      SELECT 1
      FROM users_roles ur
      WHERE ur.user_id = u.id
        AND ur.role = 'MANAGER'
  );

DELETE FROM users_roles ur
USING users u
WHERE ur.user_id = u.id
  AND u.user_type = 'BUSINESS'
  AND ur.role = 'CANDIDATE';

--rollback INSERT INTO users_roles (user_id, role) SELECT u.id, 'CANDIDATE' FROM users u WHERE u.user_type = 'BUSINESS' AND NOT EXISTS (SELECT 1 FROM users_roles ur WHERE ur.user_id = u.id AND ur.role = 'CANDIDATE');
