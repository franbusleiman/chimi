# Chimi — chatbot WhatsApp + turnero para veterinarias

SaaS multi-tenant para que una (o varias) clínicas veterinarias gestionen turnos a través de un bot de WhatsApp y un dashboard web.

## Arquitectura

```
┌──────────────────────────────────────────────────────────────────┐
│  Reverse proxy externo del server (nginx)                        │
│                                                                  │
│  Opción A (path-based, recomendada):                             │
│    gateway.liro.pet/chimi/        → frontend (puerto 18082)      │
│    gateway.liro.pet/chimi/api/    → API      (puerto 18080)      │
│    gateway.liro.pet/chimi/webhook → wpp-worker (puerto 18081)    │
│                                                                  │
│  Opción B (subdominios):                                         │
│    chimi.liro.pet                 → frontend + API               │
│    chimi-wpp.liro.pet             → wpp-worker webhook           │
└──────────────────────────────────────────────────────────────────┘
            │                  │                   │
            ▼                  ▼                   ▼
   ┌─────────────────┐ ┌─────────────────┐ ┌────────────────────┐
   │ chimi-front     │ │ chimi-api       │ │ chimi-wpp-worker   │
   │ React + nginx   │ │ Spring Boot 21  │ │ Spring Boot 21     │
   └─────────────────┘ └─────────────────┘ └────────────────────┘
                              │                   │
                              └───────┬───────────┘
                                      ▼
                              ┌──────────────────┐
                              │ MySQL 8.x        │
                              │ (externa, on-prem│
                              │  ya existente)   │
                              └──────────────────┘
```

- **chimi-api** — API REST que usa el dashboard. Lógica de negocio: turnos, bloqueos, reportes, usuarios, FAQs, config por clínica. Conexión a MySQL.
- **chimi-wpp-worker** — Recibe el webhook de WhatsApp Cloud API, corre el motor de flujo conversacional, envía mensajes salientes y recordatorios programados. Persiste estado de conversación en la misma DB.
- **chimi-front** — React + Vite servido por nginx. El path bajo el que se sirve es configurable en build-time vía `VITE_BASE_PATH`.

Multi-tenant desde día 1: cada clínica es un `tenant`, con su propio número de WhatsApp, horarios, tipos de turno, FAQs, branding y usuarios admin.

## Estructura del repo

```
chimi/
├── docker-compose.yml
├── .env.example
├── backend-api/
├── backend-wpp-worker/
├── frontend/
└── docs/
    ├── DEPLOY.md
    ├── reverse-proxy-example.conf
    └── mysql-setup.sql
```

## Deploy rápido (en el server Ubuntu)

```bash
git clone <repo> chimi && cd chimi
cp .env.example .env
# editar .env con valores reales (MySQL, JWT, WPP, paths del front)
docker compose build
docker compose up -d
docker compose logs -f
```

Después configurá tu nginx para enrutar a los puertos del compose. Ver [docs/reverse-proxy-example.conf](docs/reverse-proxy-example.conf) y [docs/DEPLOY.md](docs/DEPLOY.md) con los pasos completos.

## Checklist post-deploy

1. Crear la database/usuario en MySQL (ver `docs/mysql-setup.sql`) o usar uno existente con permisos de DDL.
2. Levantar el compose y verificar que Flyway haya corrido las migraciones (`docker compose logs chimi-api | grep -i flyway`).
3. Pegarle a tu nginx los `location` blocks del ejemplo, reload nginx.
4. Loguearse al dashboard con el usuario seed (`admin@demo.chimi / changeme` — cambialo apenas entres).
5. Dar de alta el número en Meta Business con webhook apuntando a tu URL pública.
6. Cargar `wpp_phone_number_id` y `wpp_business_account_id` en la tabla `tenants` del demo.

## Desarrollo local

Para correr sin Docker:

```bash
# Backend API
cd backend-api && mvn spring-boot:run

# WPP Worker
cd backend-wpp-worker && mvn spring-boot:run

# Frontend (dev server)
cd frontend && npm install && npm run dev
```

Para dev con subpath, exportá `VITE_BASE_PATH` antes de `npm run dev`:

```bash
VITE_BASE_PATH=/chimi/ VITE_API_BASE_URL=http://localhost:8080/api npm run dev
```
