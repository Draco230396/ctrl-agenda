# spiid-iam-service

Servicio de Identidad y Acceso (IAM) para SPIID: **registro**, **login**, emisión de **Access Token (JWT)** y **Refresh Token** (persistido y revocable).

> Arquitectura: **Hexagonal** (domain / application / infrastructure)

---

## Responsabilidad

- Registro de usuarios (`iam.user_account`)
- Asignación de roles por catálogo (`iam.catalog_role`, `iam.user_role`)
- Login y emisión de tokens
- Refresh token con rotación (revoca el anterior)
- Endpoints de catálogos para UI (roles)
- Endpoint `/auth/me` para obtener la sesión actual

---

## Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL (BD `spiid` con schema `iam` ya creado)
- IntelliJ IDEA

---

## Base de datos (PostgreSQL)

Este servicio trabaja sobre el schema `iam` en la BD `spiid`.

Tablas esperadas (ya creadas por tus scripts):
- `iam.catalog_role`
- `iam.user_account`
- `iam.user_role`
- `iam.refresh_token`

> En `application.yml` se usa `ddl-auto: validate` para **validar** (no crear) tablas.

---

## Cómo levantar (local)

### IntelliJ IDEA
1. **File > Open** y selecciona este repo.
2. Configura **Project SDK = Java 21**.
3. Ejecuta `IamServiceApplication`.

### Terminal
```bash
mvn clean spring-boot:run
```

Servicio en:
- http://localhost:8080

Swagger:
- http://localhost:8080/swagger-ui/index.html

---

## Endpoints

### Registro
`POST /api/v1/auth/register`

Body:
```json
{
  "email": "romeo@test.com",
  "password": "123456",
  "roleCodes": [1, 2]
}
```

> `roleCodes` son números del catálogo `iam.catalog_role`.
> El response ya incluye **code/key/description** para mostrar en UI.

### Login
`POST /api/v1/auth/login`

### Refresh
`POST /api/v1/auth/refresh`

### Me (requiere access token)
`GET /api/v1/auth/me`

Header:
`Authorization: Bearer <accessToken>`

### Catálogo de roles
`GET /api/v1/catalog/roles`

---

## cURL de prueba

Registro:
```bash
curl -X POST http://localhost:8080/api/v1/auth/register   -H "Content-Type: application/json"   -d '{"email":"romeo@test.com","password":"123456","roleCodes":[1,2]}'
```

Login:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login   -H "Content-Type: application/json"   -d '{"email":"romeo@test.com","password":"123456"}'
```

Me (con token):
```bash
curl http://localhost:8080/api/v1/auth/me   -H "Authorization: Bearer <accessToken>"
```

---

## Notas de seguridad

- Password: se hashea con **BCrypt** en la app (no en la DB)
- Refresh token: se guarda como **hash SHA-256** (`iam.refresh_token.token_hash`) y se puede revocar.
- En prod: `app.security.jwt.secret` debe ir como variable de entorno y ser largo/aleatorio.
