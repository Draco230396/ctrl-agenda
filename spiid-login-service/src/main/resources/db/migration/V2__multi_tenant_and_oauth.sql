-- =========================================
-- V2: Multi-tenant + OAuth (Google) support
-- =========================================

CREATE SCHEMA IF NOT EXISTS login;
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =========================================
-- 1. TENANT (NUEVO)
-- =========================================

CREATE TABLE IF NOT EXISTS login.tenant (
id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
name VARCHAR(150) NOT NULL,
active BOOLEAN NOT NULL DEFAULT TRUE,
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =========================================
-- 2. USER_ACCOUNT (MODIFICACIONES)
-- =========================================

-- Agregar tenant_id si no existe
ALTER TABLE login.user_account
ADD COLUMN IF NOT EXISTS tenant_id UUID;

-- Agregar provider (Google / Local)
ALTER TABLE login.user_account
ADD COLUMN IF NOT EXISTS provider VARCHAR(20) DEFAULT 'LOCAL';

-- Agregar provider_id (ID único de Google)
ALTER TABLE login.user_account
ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255);

-- Permitir password NULL (Google login)
ALTER TABLE login.user_account
ALTER COLUMN password_hash DROP NOT NULL;

-- Crear tenants para usuarios existentes (solo si no tienen)
UPDATE login.user_account
SET tenant_id = gen_random_uuid()
WHERE tenant_id IS NULL;

-- Ahora hacerlo obligatorio
ALTER TABLE login.user_account
ALTER COLUMN tenant_id SET NOT NULL;

-- FK hacia tenant
ALTER TABLE login.user_account
ADD CONSTRAINT fk_user_tenant
FOREIGN KEY (tenant_id) REFERENCES login.tenant(id);

-- =========================================
-- 3. ROLES (AJUSTE A SaaS)
-- =========================================

-- Limpiar roles viejos (opcional)
-- DELETE FROM login.catalog_role;

-- Insertar roles correctos para SaaS
INSERT INTO login.catalog_role (code, key, description) VALUES
(1,'OWNER','Dueño del negocio'),
(2,'STAFF','Empleado del negocio'),
(3,'CLIENT','Cliente'),
(9,'ADMIN','Administrador global')
ON CONFLICT (code) DO NOTHING;

-- =========================================
-- 4. ÍNDICES (PERFORMANCE)
-- =========================================

CREATE INDEX IF NOT EXISTS idx_user_tenant
ON login.user_account(tenant_id);

CREATE INDEX IF NOT EXISTS idx_user_provider
ON login.user_account(provider, provider_id);

-- =========================================
-- 5. REFRESH TOKEN (MEJORA OPCIONAL)
-- =========================================

ALTER TABLE login.refresh_token
ADD COLUMN IF NOT EXISTS tenant_id UUID;

CREATE INDEX IF NOT EXISTS idx_refresh_tenant
ON login.refresh_token(tenant_id);

-- =========================================
-- FIN MIGRACIÓN
-- =========================================
