-- =========================================
-- SCHEMA
-- =========================================
CREATE SCHEMA IF NOT EXISTS login;
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =========================================
-- TENANT
-- =========================================
CREATE TABLE login.tenant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
);

-- =========================================
-- ROLE CATALOG
-- =========================================
CREATE TABLE login.catalog_role (
    code SMALLINT PRIMARY KEY,
    key VARCHAR(40) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO login.catalog_role (code, key, description) VALUES
(1,'OWNER','Dueño del negocio'),
(2,'STAFF','Empleado del negocio'),
(3,'CLIENT','Cliente'),
(9,'ADMIN','Administrador global')
ON CONFLICT DO NOTHING;

-- =========================================
-- USER ACCOUNT
-- =========================================
CREATE TABLE login.user_account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(200),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    provider_id VARCHAR(255),

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_user_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES login.tenant(id),

    CONSTRAINT chk_provider
        CHECK (provider IN ('LOCAL','GOOGLE'))
);

-- índices
CREATE UNIQUE INDEX uq_user_email ON login.user_account(email);
CREATE UNIQUE INDEX uq_google_user
ON login.user_account(provider_id)
WHERE provider = 'GOOGLE';

CREATE INDEX idx_user_tenant ON login.user_account(tenant_id);

-- =========================================
-- REFRESH TOKEN
-- =========================================
CREATE TABLE login.refresh_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL,

    token_hash CHAR(64) NOT NULL,
    ip_address VARCHAR(255),
    user_agent VARCHAR(255),

    issued_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_refresh_user
        FOREIGN KEY (user_id)
        REFERENCES login.user_account(id),

    CONSTRAINT fk_refresh_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES login.tenant(id)
);

CREATE UNIQUE INDEX uq_refresh_hash ON login.refresh_token(token_hash);
CREATE INDEX idx_refresh_user ON login.refresh_token(user_id);

-- =========================================
-- TENANT MEMBERSHIP
-- =========================================
CREATE TABLE login.tenant_membership (
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

CREATE INDEX idx_tm_user ON login.tenant_membership(user_id);

-- =========================================
-- USER ROLE (SI USAS @ManyToMany)
-- =========================================
CREATE TABLE login.user_role (
    user_id UUID NOT NULL,
    role_code SMALLINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (user_id, role_code),

    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES login.user_account(id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_code) REFERENCES login.catalog_role(code)
);