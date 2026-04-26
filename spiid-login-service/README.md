# spiid-login-service

Servicio de **Identidad y Acceso (IAM)** para SPIID.

Este módulo es responsable de gestionar la **autenticación, autorización y multi-tenancy**, incluyendo login con credenciales y **Google OAuth**, emisión de **JWT**, manejo de **Refresh Tokens seguros** y control de acceso por roles.

> Arquitectura: **Hexagonal (Clean Architecture)**
> Separación en: `domain / application / infrastructure`

---

# 🎯 Objetivo del módulo

Centralizar toda la lógica de identidad del sistema SPIID:

* Autenticación de usuarios (local + Google)
* Autorización basada en roles
* Manejo de sesiones con JWT
* Multi-tenant (usuarios pertenecen a organizaciones)
* Seguridad de tokens (rotación, revocación)

---

# 🧠 ¿Qué problema resuelve?

Antes de este servicio:

❌ No hay control centralizado de usuarios
❌ No hay autenticación segura
❌ No hay separación por tenant (SaaS)
❌ No hay control de sesiones

Con este servicio:

✔️ Login seguro (JWT + Refresh Token)
✔️ Multi-tenant real (SaaS ready)
✔️ OAuth (Google) integrado
✔️ Roles dinámicos desde DB
✔️ Tokens revocables y auditables

---

# 🧱 Arquitectura

## 🔷 Hexagonal (Ports & Adapters)

```
application (casos de uso)
        ↓
domain (reglas de negocio)
        ↓
ports (interfaces)
        ↓
infrastructure (DB, JWT, Google)
```

---

## 🔄 Flujo principal (login)

```
AuthController
    ↓
AuthService (application)
    ↓
GoogleTokenVerifierPort / UserRepositoryPort
    ↓
Infrastructure (DB / Google / JWT)
```

---

## 🔄 Flujo catálogo (roles)

```
AuthService
↓
CatalogUseCase
↓
RoleCatalogRepositoryPort
↓
DB (catalog_role)
↓
RoleCatalogItem
```

---

# ⚙️ Tecnologías

* Java 21
* Spring Boot 3.5.x
* Spring Security
* JWT (JJWT)
* PostgreSQL
* Google OAuth (ID Token)
* Maven

---

# 🗄️ Base de datos

Schema: `login`

Tablas principales:

* `tenant` → organización
* `user_account` → usuarios
* `catalog_role` → catálogo de roles
* `tenant_membership` → relación usuario-tenant
* `user_role` → roles asignados
* `refresh_token` → sesiones seguras

---

# 🔐 Seguridad

## Access Token (JWT)

* Stateless
* Contiene:

    * userId
    * tenantId
    * roles

## Refresh Token

* Persistido en DB
* Guardado como **hash (SHA-256)**
* Rotación automática
* Revocable

---

# 🔑 Autenticación soportada

## 1. Local

* Email + password (BCrypt)

## 2. Google OAuth

* Validación con `GoogleIdTokenVerifier`
* Se obtiene:

    * email
    * name
    * googleId

---

# 🚀 Cómo levantar el proyecto

## IntelliJ

1. Abrir proyecto
2. Configurar:

    * Java 21
3. Ejecutar:

```
SpiidLoginServiceApplication
```

---

## Terminal

```bash
mvn clean install
mvn -pl spiid-login-service spring-boot:run
```

---

# 🌐 URLs

* API: http://localhost:8080
* Swagger: http://localhost:8080/swagger-ui/index.html

---

# 📡 Endpoints

## 🔐 Registro

`POST /api/v1/auth/register`

```json
{
  "email": "user@test.com",
  "password": "123456",
  "roleCodes": [1]
}
```

---

## 🔐 Login (local)

`POST /api/v1/auth/login`

---

## 🔐 Login (Google)

`POST /api/v1/auth/google`

```json
{
  "idToken": "GOOGLE_ID_TOKEN",
  "tenantId": "uuid",
  "role": "CLIENT"
}
```

---

## 🔁 Refresh Token

`POST /api/v1/auth/refresh`

---

## 👤 Usuario actual

`GET /api/v1/auth/me`

Header:

```
Authorization: Bearer <accessToken>
```

---

## 📚 Catálogo de roles

`GET /api/v1/catalog/roles`

---

# 🧪 cURL de prueba

## Registro

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
-H "Content-Type: application/json" \
-d '{"email":"user@test.com","password":"123456","roleCodes":[1]}'
```

---

## Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
-H "Content-Type: application/json" \
-d '{"email":"user@test.com","password":"123456"}'
```

---

## Me

```bash
curl http://localhost:8080/api/v1/auth/me \
-H "Authorization: Bearer <accessToken>"
```

---

# ⚠️ Configuración importante

## application.yml

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate

app:
  security:
    jwt:
      secret: ${JWT_SECRET}
```

---

# 🧠 Buenas prácticas aplicadas

✔️ Clean Architecture
✔️ Separación de responsabilidades
✔️ Seguridad por diseño
✔️ Tokens seguros (hash + rotación)
✔️ Multi-tenant real
✔️ Catálogos desacoplados

---

# 🚀 Evolución futura

* Refresh token reuse detection
* MFA (2FA)
* OAuth adicional (Apple, Facebook)
* Auditoría completa (login logs)
* Rate limiting

---

# 🧾 Conclusión

Este módulo no es solo login:

👉 Es el **núcleo de identidad del sistema SPIID**

Permite:

* Escalar a SaaS
* Integrar múltiples clientes
* Asegurar accesos
* Controlar sesiones de forma profesional

---
