-- =========================================
-- V5: AaaS Multi-tenant + Google OAuth (FINAL REAL)
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
-- 2. ROLE CATALOG
-- =========================================

CREATE TABLE IF NOT EXISTS login.catalog_role (
    code SMALLINT PRIMARY KEY,
    key VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(150)
);

INSERT INTO login.catalog_role (code, key, description) VALUES
(1,'OWNER','Dueño del negocio'),
(2,'STAFF','Empleado del negocio'),
(3,'CLIENT','Cliente'),
(9,'ADMIN','Administrador global')
ON CONFLICT (code) DO NOTHING;

-- =========================================
-- 3. USER_ACCOUNT
-- =========================================

CREATE TABLE IF NOT EXISTS login.user_account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_user_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES login.tenant(id)
);

-- normalizar emails
UPDATE login.user_account
SET email = LOWER(email);

-- =========================================
-- CREAR TENANT POR CADA USUARIO SIN TENANT
-- =========================================

DO $$
DECLARE
    u RECORD;
    new_tenant_id UUID;
BEGIN
    FOR u IN SELECT id FROM login.user_account WHERE tenant_id IS NULL LOOP

        INSERT INTO login.tenant (id, name)
        VALUES (gen_random_uuid(), 'Migrated Tenant')
        RETURNING id INTO new_tenant_id;

        UPDATE login.user_account
        SET tenant_id = new_tenant_id
        WHERE id = u.id;

    END LOOP;
END $$;

-- ahora obligatorio
ALTER TABLE login.user_account
ALTER COLUMN tenant_id SET NOT NULL;

-- =========================================
-- 🔐 CONSTRAINTS USUARIOS
-- =========================================

-- email único global
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_email
ON login.user_account(email);

-- Google único
CREATE UNIQUE INDEX IF NOT EXISTS uq_google_user
ON login.user_account(provider_id)
WHERE provider = 'GOOGLE';

-- índices
CREATE INDEX IF NOT EXISTS idx_user_tenant
ON login.user_account(tenant_id);

CREATE INDEX IF NOT EXISTS idx_user_provider
ON login.user_account(provider, provider_id);

-- =========================================
-- 4. REFRESH TOKEN
-- =========================================

CREATE TABLE IF NOT EXISTS login.refresh_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    token_hash TEXT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_at TIMESTAMPTZ,

    CONSTRAINT fk_refresh_user
        FOREIGN KEY (user_id)
        REFERENCES login.user_account(id),

    CONSTRAINT fk_refresh_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES login.tenant(id)
);

-- índices tokens
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

    CONSTRAINT fk_tm_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES login.tenant(id),

    CONSTRAINT fk_tm_user
        FOREIGN KEY (user_id)
        REFERENCES login.user_account(id),

    CONSTRAINT fk_tm_role
        FOREIGN KEY (role_code)
        REFERENCES login.catalog_role(code),

    CONSTRAINT uq_tm UNIQUE (tenant_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_tm_user
ON login.tenant_membership(user_id);

CREATE INDEX IF NOT EXISTS idx_tm_tenant
ON login.tenant_membership(tenant_id);

-- =========================================
-- 6. DATOS INICIALES OPCIONALES (ADMIN)
-- =========================================

-- puedes usar esto para crear un super admin global si quieres
-- INSERT INTO login.user_account (...)

-- =========================================
-- FIN
-- =========================================