# Despliegue de Chimi en tu server on-prem

Server: Ubuntu 20.04.6 LTS · aarch64 · Docker 28.x · Docker Compose v2.33.

## 1. Pre-requisitos

- Docker + Docker Compose v2 (ya los tenés).
- Reverse proxy con HTTPS válido (nginx) andando en el server.
- Acceso a tu MySQL desde la red del server.
- **URL pública para el dashboard y el webhook**. Dos opciones:
  - **A (path-based, recomendada)**: usar tu dominio existente bajo un subpath, ej.
    `https://gateway.liro.pet/chimi/` para el dashboard y `https://gateway.liro.pet/chimi/webhook` para el webhook.
    No requiere crear subdominios nuevos ni certificados extra.
  - **B (subdominios)**: `chimi.liro.pet` y `chimi-wpp.liro.pet`. Requiere DNS + certs.

## 2. Preparar MySQL

1. Conectate como root (o admin) a tu MySQL.
2. Ejecutá [`docs/mysql-setup.sql`](mysql-setup.sql) (cambiá la password antes).
3. Anotá: host, puerto (3306), database (`chimi`), usuario, password.

Conectividad MySQL ↔ contenedor:
- **MySQL en el mismo host que Docker**: poné `MYSQL_HOST=host.docker.internal` y
  descomentá los `extra_hosts:` en `docker-compose.yml`.
- **MySQL en otra máquina**: usá hostname/IP real. Verificá que `bind-address`
  no esté en `127.0.0.1` y que el firewall deje pasar `3306`.

## 3. Clonar y configurar

```bash
git clone <repo> chimi
cd chimi
cp .env.example .env
nano .env
```

Variables críticas:

| Variable | Cómo se completa |
|---|---|
| `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD` | Tu MySQL |
| `JWT_SECRET` | `openssl rand -base64 48` |
| `INTERNAL_API_KEY` | `openssl rand -base64 32` |
| `WPP_VERIFY_TOKEN` | Inventalo, vas a usar el mismo string en Meta |
| `WPP_ACCESS_TOKEN`, `WPP_PHONE_NUMBER_ID`, `WPP_BUSINESS_ACCOUNT_ID` | Cuando los tengas de Meta |
| `VITE_BASE_PATH` | `/chimi/` si opción A · `/` si opción B |
| `VITE_API_BASE_URL` | `/chimi/api` si opción A · `/api` si opción B |

## 4. Build + run

```bash
docker compose build              # primera build ~5-10 min
docker compose up -d
docker compose ps                 # los 3 servicios "healthy"
docker compose logs -f chimi-api  # buscar "Successfully applied 2 migrations"
```

Verificación local (desde el server):

```bash
curl http://localhost:18080/actuator/health     # API → {"status":"UP"}
curl http://localhost:18081/actuator/health     # worker → {"status":"UP"}
curl -I http://localhost:18082/                 # front → 200
```

## 5. Configurar tu nginx existente

Ver [`docs/reverse-proxy-example.conf`](reverse-proxy-example.conf). Tenés las dos opciones documentadas.

### Para opción A (path-based bajo `gateway.liro.pet/chimi/`)

Editá tu config de nginx (el archivo donde ya está el `server { server_name gateway.liro.pet ... }`)
y agregale **dentro del `server` block existente** los `location` que están en el ejemplo:

```nginx
location = /chimi/webhook { proxy_pass http://127.0.0.1:18081/webhook; ... }
location /chimi/api/      { proxy_pass http://127.0.0.1:18080/api/;    ... }
location /chimi/api/internal/ { return 404; }
location /chimi/          { proxy_pass http://127.0.0.1:18082/;        ... }
```

Lo crucial:

- El trailing slash en `proxy_pass http://127.0.0.1:18082/;` **strippea** el prefijo `/chimi/`.
  Sin ese trailing slash, el contenedor del front recibe `/chimi/...` y no encuentra los assets.
- `location = /chimi/webhook` con el `=` fuerza match exacto.
- El order de los `location` no importa: nginx elige el prefijo más largo.

Reload y prueba:

```bash
sudo nginx -t && sudo systemctl reload nginx

# Desde tu compu (no el server):
curl -I  https://gateway.liro.pet/chimi/                       # 200 + HTML
curl -sS https://gateway.liro.pet/chimi/api/actuator/health    # {"status":"UP"}
curl -i  "https://gateway.liro.pet/chimi/webhook?hub.mode=subscribe&hub.verify_token=$WPP_VERIFY_TOKEN&hub.challenge=hola"
# Esperás: 200 + body "hola"
```

### Para opción B (subdominios)

Misma idea pero con dos `server` blocks. Hay que generar los certs:

```bash
sudo certbot certonly --nginx -d chimi.liro.pet
sudo certbot certonly --nginx -d chimi-wpp.liro.pet
```

Y en el `.env` poner `VITE_BASE_PATH=/` + `VITE_API_BASE_URL=/api`, **y rebuildear** el front
(`docker compose build chimi-front && docker compose up -d`).

## 6. Alta del webhook en Meta

1. Meta for Developers → tu app → WhatsApp → Configuration.
2. Webhook callback URL:
   - Opción A → `https://gateway.liro.pet/chimi/webhook`
   - Opción B → `https://chimi-wpp.liro.pet/webhook`
3. Verify token: el valor de `WPP_VERIFY_TOKEN`.
4. Subscribite al evento `messages`.
5. Anotá el `phone_number_id` y el WABA ID, ponelos en `.env`, `docker compose restart chimi-wpp-worker`.
6. Linkeá el número con el tenant `demo` en la DB:

```sql
UPDATE tenants
   SET wpp_phone_number_id     = '<tu phone_number_id>',
       wpp_business_account_id = '<tu WABA ID>'
 WHERE slug = 'demo';
```

## 7. Primer login

- URL: `https://gateway.liro.pet/chimi/` (opción A) o `https://chimi.liro.pet/` (opción B).
- Clínica: `demo`
- Email: `admin@demo.chimi`
- Password: `changeme` *(si no entra, regenerá el hash — ver más abajo)*

Cambiar password manual:

```bash
sudo apt install -y apache2-utils
HASH=$(htpasswd -bnBC 10 "" "nuevaPassword" | tr -d ':\n')
mysql -h $MYSQL_HOST -u $MYSQL_USER -p chimi -e \
  "UPDATE app_users SET password_hash='$HASH' WHERE email='admin@demo.chimi';"
```

## 8. Updates posteriores

```bash
git pull
docker compose build
docker compose up -d
```

Flyway aplica solo las migraciones nuevas. Nunca edites una migración ya aplicada.

## Troubleshooting

- **`Communications link failure`** → el contenedor no llega a MySQL. Verificá `MYSQL_HOST`, los `extra_hosts:` y el `bind-address` de MySQL.
- **`Access denied for user`** → usuario, password o el `host` permitido en MySQL (`'chimi_app'@'%'`).
- **`Public Key Retrieval is not allowed`** → ya está cubierto con `allowPublicKeyRetrieval=true` en la URL. Si persiste, cambiá el plugin de auth del usuario a `mysql_native_password`.
- **`Table 'chimi.xxx' doesn't exist`** → Flyway no corrió. `docker compose logs chimi-api | grep -i flyway`.
- **El dashboard muestra "Cannot GET /assets/..."** → te olvidaste el trailing slash en `proxy_pass` del nginx externo, o `VITE_BASE_PATH` no matchea el path del nginx.
- **Meta no valida el webhook** → SSL inválido o el verify token no matchea.
- **El bot no responde** → el `phone_number_id` no está cargado en el tenant correspondiente.
