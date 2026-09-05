# Развёртывание PixelForge

Прод крутится на `80.87.201.25` в `/opt/pixelforge`, домен
`pixelforge.tirskix.space`.

## Архитектура

| Компонент | Где | Порт (localhost) |
|-----------|-----|------------------|
| frontend (nginx + собранный Vite + статический Snap!) | контейнер `frontend` | `8181` |
| backend (Spring Boot, Java 21) | контейнер `backend` | `8180` |
| PostgreSQL 16 / Redis 7 / MinIO | контейнеры | только внутри compose-сети |
| Внешний nginx на хосте | `pixelforge.tirskix.space` → `127.0.0.1:8181` | 80/443 |

GDevelop не разворачивается — используется существующий
`https://gdevelop.tirskix.space`.

## Первичная установка

```bash
cd /opt
git clone https://github.com/AspWinCode/pixelforge
cd pixelforge
cp .env.example .env
# заполнить .env реальными паролями
docker compose build
docker compose up -d

# внешний nginx
cp deploy/nginx/pixelforge.tirskix.space.conf /etc/nginx/sites-available/pixelforge.tirskix.space
ln -s /etc/nginx/sites-available/pixelforge.tirskix.space /etc/nginx/sites-enabled/
nginx -t && systemctl reload nginx
certbot --nginx -d pixelforge.tirskix.space
```

## Обновление

```bash
cd /opt/pixelforge
git pull
docker compose build
docker compose up -d
```

## Интеграция с кабинетом ученика (learning-portal)

PixelForge реализует тот же контракт, что и другие площадки (КОДЭКС):

| Направление | Эндпоинт |
|---|---|
| Вход по SSO из кабинета | `GET /api/auth/sso?token=<JWT>` (HS256, `aud=pixelforge`) |
| Прогресс ученика для тренера/методиста | `GET /api/internal/lms-progress/lp-student-{id}` (заголовок `X-LP-Signature`) |
| Обратный пуш прогресса | `POST {PORTAL_BASE_URL}/api/v1/student-portal/progress-sync` (`X-Kodex-Signature`, `catalog_item_code=pixelforge`) |
| Студия методиста (authoring) | `/api/admin/**` — HMAC `X-LP-Signature=hex(HMAC_SHA256(secret,"METHOD\npath\nts\nsha256(body)"))` + `X-LP-Timestamp` (±300с) |
| Вебхук публикации курса | `POST {PORTAL_BASE_URL}/api/v1/pixelforge/courses/webhook` (`X-LP-Signature=hex(HMAC_SHA256(secret, raw_body))`) |

Переменные окружения (`.env`):

* `SSO_KODEX_SHARED_SECRET` — общий HS256-секрет (тот же, что у портала для
  КОДЭКС). Пустой = интеграция выключена.
* `PORTAL_BASE_URL` — база кабинета для обратного пуша (по умолчанию
  `https://tirskix.space`).

На стороне портала: пункт витрины `catalog_item.code = "pixelforge"` c
`external_url = https://pixelforge.tirskix.space/api/auth/sso`, переменная
`PIXELFORGE_BASE_URL`, затем `alembic upgrade head`.

## Заметки

* `.env` на сервере не коммитится.
* LMS SSO работает только после подстановки реальных `LMS_JWT_SECRET` и
  `LMS_WEBHOOK_SECRET` (сейчас заглушки).
* Начальные данные: `pixelforge-backend/db-seed.sql` — при необходимости
  `docker compose exec -T postgres psql -U pixelforge pixelforge < pixelforge-backend/db-seed.sql`.
