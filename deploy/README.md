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

## Заметки

* `.env` на сервере не коммитится.
* LMS SSO работает только после подстановки реальных `LMS_JWT_SECRET` и
  `LMS_WEBHOOK_SECRET` (сейчас заглушки).
* Начальные данные: `pixelforge-backend/db-seed.sql` — при необходимости
  `docker compose exec -T postgres psql -U pixelforge pixelforge < pixelforge-backend/db-seed.sql`.
