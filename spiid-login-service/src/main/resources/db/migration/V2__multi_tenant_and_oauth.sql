-- =========================================
-- V4: AaaS Multi-tenant + Google OAuth (FINAL)
-- =========================================

CREATE SCHEMA IF NOT EXISTS login;
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =========================================
-- 1. TENANT
-- =========================================

CREATE TABLE IF NOT EXISTS login.tenant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =========================================
-- 2. USER_ACCOUNT
-- =========================================

ALTER TABLE login.user_account
ADD COLUMN IF NOT EXISTS tenant_id UUID;

ALTER TABLE login.user_account
ADD COLUMN IF NOT EXISTS provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';

ALTER TABLE login.user_account
ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255);

ALTER TABLE login.user_account
ALTER COLUMN password_hash DROP NOT NULL;

-- normalizar emails
UPDATE login.user_account
SET email = LOWER(email);

-- =========================================
-- 🔥 CREAR TENANTS REALES PARA USUARIOS EXISTENTES
-- =========================================

INSERT INTO login.tenant (id, name)
SELECT DISTINCT gen_random_uuid(), 'Migrated Tenant'
FROM login.user_account
WHERE tenant_id IS NULL;

-- asignar tenant válido
UPDATE login.user_account u
SET tenant_id = t.id
FROM login.tenant t
WHERE u.tenant_id IS NULL
LIMIT 1;

-- obligatorio
ALTER TABLE login.user_account
ALTER COLUMN tenant_id SET NOT NULL;

-- FK
ALTER TABLE login.user_account
ADD CONSTRAINT fk_user_tenant
FOREIGN KEY (tenant_id) REFERENCES login.tenant(id);

-- =========================================
-- 🔐 CONSTRAINTS CRÍTICOS
-- =========================================

-- email único global
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_email
ON login.user_account(email);

-- Google único (solo si provider = GOOGLE)
CREATE UNIQUE INDEX IF NOT EXISTS uq_google_user
ON login.user_account(provider_id)
WHERE provider = 'GOOGLE';

-- índice tenant
CREATE INDEX IF NOT EXISTS idx_user_tenant
ON login.user_account(tenant_id);

-- índice provider
CREATE INDEX IF NOT EXISTS idx_user_provider
ON login.user_account(provider, provider_id);

-- =========================================
-- 3. ROLES
-- =========================================

INSERT INTO login.catalog_role (code, key, description) VALUES
(1,'OWNER','Dueño del negocio'),
(2,'STAFF','Empleado del negocio'),
(3,'CLIENT','Cliente'),
(9,'ADMIN','Administrador global')
ON CONFLICT (code) DO NOTHING;

-- =========================================
-- 4. REFRESH TOKEN (SEGURO)
-- =========================================

ALTER TABLE login.refresh_token
ADD COLUMN IF NOT EXISTS tenant_id UUID;

ALTER TABLE login.refresh_token
ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT now();

ALTER TABLE login.refresh_token
ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMPTZ;

ALTER TABLE login.refresh_token
ADD CONSTRAINT fk_refresh_tenant
FOREIGN KEY (tenant_id) REFERENCES login.tenant(id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_refresh_hash
ON login.refresh_token(token_hash);

CREATE INDEX IF NOT EXISTS idx_refresh_user
ON login.refresh_token(user_id);

CREATE INDEX IF NOT EXISTS idx_refresh_tenant
ON login.refresh_token(tenant_id);

-- =========================================
-- 5. TENANT MEMBERSHIP (SaaS REAL)
-- =========================================

CREATE TABLE IF NOT EXISTS login.tenant_membership (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role_code SMALLINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_tm_tenant FOREIGN KEY (tenant_id) REFERENCES login.tenant(id),
    CONSTRAINT fk_tm_user FOREIGN KEY (user_id) REFERENCES login.user_account(id),
    CONSTRAINT fk_tm_role FOREIGN KEY (role_code) REFERENCES login.catalog_role(code),

    CONSTRAINT uq_tm UNIQUE (tenant_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_tm_user
ON login.tenant_membership(user_id);

CREATE INDEX IF NOT EXISTS idx_tm_tenant
ON login.tenant_membership(tenant_id);

-- =========================================
-- FIN
-- =========================================