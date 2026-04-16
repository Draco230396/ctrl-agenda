# ctrl-agenda
Este proyecto es para gestionar y vender servicios para los siguientes Pymes: Estética, Barberia, y todo lo relacionado de ventas de servicios y/o productos

# 🚀 SPIID SaaS Platform

> Plataforma SaaS multi-tenant para gestión de citas, servicios y negocios
> Diseñada para escalar como producto comercial real.

---

# 🧠 VISIÓN DEL PROYECTO

SPIID permite a cualquier negocio basado en servicios (barberías, clínicas, estética, consultorios, etc.):

* Gestionar clientes
* Configurar servicios
* Administrar agenda
* Recibir citas
* Automatizar procesos
* Escalar digitalmente

---

# 🏗️ ARQUITECTURA GENERAL

```text
Cliente (Web / Mobile)
        ↓
API Gateway (futuro)
        ↓
---------------------------------------------------------
| IAM Service            → Autenticación y seguridad     |
| Business Service       → Negocios (tenants)            |
| Staff Service          → Empleados / profesionales     |
| Service Catalog        → Servicios del negocio         |
| Availability Service   → Horarios y disponibilidad     |
| Appointment Service    → Citas                         |
| Notification Service   → Mensajes y recordatorios      |
| Billing Service        → Suscripciones y pagos         |
| Analytics Service      → Métricas y reportes           |
---------------------------------------------------------
        ↓
PostgreSQL (una base por servicio)
```

---

# 🧩 PRINCIPIOS CLAVE

## 🔐 Multi-Tenancy

* Cada negocio = 1 `tenantId`
* Todos los datos están aislados por tenant
* Cada request lleva el `tenantId` en el JWT

---

## 🧱 Arquitectura Hexagonal

Cada microservicio sigue:

```text
domain/
application/
infrastructure/
```

---

# 🔐 IAM SERVICE (spiid-login-service)

📦 `com.spiid.login.service`

---

## 🧠 Responsabilidad

Sistema central de identidad:

* Registro de usuarios
* Login (email/password)
* Login con Google (OAuth)
* Refresh tokens
* Roles
* Multi-tenant (`tenantId`)
* Generación de JWT

---

## 🔑 FUNCIONALIDADES IMPLEMENTADAS

### ✅ Autenticación

* JWT con:

    * `sub` (email)
    * `roles`
    * `tenantId`

---

### ✅ Google Login

Flujo:

```text
Frontend → Google
        → ID Token
        → IAM valida token
        → busca usuario
            → existe → login
            → no existe → crea usuario
        → genera JWT propio
```

---

### ✅ Multi-Tenant

* OWNER crea `tenantId`
* CLIENT se une a tenant existente
* JWT siempre incluye `tenantId`

---

### ✅ Catálogo de Roles

Los roles NO se crean manualmente.

Se obtienen desde:

```text
CatalogService → RoleCatalogRepository → DB
```

---

## 📂 ESTRUCTURA INTERNA

---

### 📁 domain

```text
com.spiid.login.service.domain
```

#### model

* `User`
* `RoleCatalogItem`

#### valueobject

* `RoleCode`

#### port

* `port.in` → AuthUseCase, CatalogUseCase
* `port.out` → UserRepositoryPort, RoleCatalogRepositoryPort, GoogleTokenVerifierPort

---

### 📁 application

```text
com.spiid.login.service.application
```

#### usecase

* `AuthService`
* `CatalogService`

---

### 📁 infrastructure

```text
com.spiid.login.service.infrastructure
```

#### inbound (REST)

* `AuthController`

#### outbound (DB)

* `UserRepositoryAdapter`
* `RoleCatalogRepositoryAdapter`

#### security

* `JwtAuthFilter`
* `JwtTokenService`
* `GoogleTokenVerifierAdapter`
* `TenantContext`

#### config

* `SecurityConfig`

---

# 🔄 FLUJO DE AUTENTICACIÓN

---

## 🧾 Registro/Login normal

```text
User → IAM
     → valida credenciales
     → genera JWT
```

---

## 🌐 Login con Google

```text
User → Google
     → IAM valida token
     → crea usuario si no existe
     → asigna tenantId
     → obtiene rol desde catálogo
     → genera JWT
```

---

# 🔐 JWT STRUCTURE

```json
{
  "sub": "user@email.com",
  "roles": ["OWNER"],
  "tenantId": "UUID"
}
```

---

# 🧠 CONTEXTO MULTI-TENANT

Cada request:

```text
JwtAuthFilter → extrae tenantId
              → TenantContext.set()
```

Uso:

```java
UUID tenantId = TenantContext.get();
```

---

# 🏢 BUSINESS SERVICE (Próximo)

📦 `com.spiid.businessservice`

Responsable de:

* Crear negocio
* Configuración del tenant
* Datos del negocio

---

# 👨‍💼 STAFF SERVICE

📦 `com.spiid.staffservice`

* Gestión de empleados
* Asignación de servicios

---

# 💼 SERVICE CATALOG

📦 `com.spiid.serviceservice`

* Definición de servicios
* Precio
* Duración

---

# 🕒 AVAILABILITY SERVICE

📦 `com.spiid.availabilityservice`

* Horarios
* Bloqueos
* Slots disponibles

---

# 📅 APPOINTMENT SERVICE

📦 `com.spiid.appointmentservice`

* Crear citas
* Validar disponibilidad
* Cancelar / reprogramar

---

# 📩 NOTIFICATION SERVICE

📦 `com.spiid.notificationservice`

* Emails
* WhatsApp
* Recordatorios

---

# 💳 BILLING SERVICE

📦 `com.spiid.billingservice`

* Suscripciones SaaS
* Pagos
* Planes

---

# 📊 ANALYTICS SERVICE

📦 `com.spiid.analyticsservice`

* Métricas
* KPI
* Reportes

---

# 🌐 API GATEWAY (FUTURO)

📦 `com.spiid.apigateway`

* Validación JWT
* Routing
* Rate limiting

---

# 🚀 ROADMAP

## Fase 1

* IAM multi-tenant ✅
* Google Login ✅

## Fase 2

* Business Service
* Staff Service
* Service Catalog

## Fase 3

* Appointment + Availability

## Fase 4

* API Gateway

## Fase 5

* Notificaciones

## Fase 6

* Billing + Analytics

---

# 🧠 FILOSOFÍA

Este proyecto está diseñado como:

* SaaS comercial real
* Arquitectura escalable
* Sistema modular
* Seguridad robusta

---

# 🔥 CONCLUSIÓN

SPIID no es solo un sistema de citas.

Es una plataforma SaaS completa para digitalizar negocios de servicios a gran escala.

---
