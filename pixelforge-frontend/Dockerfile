# syntax=docker/dockerfile:1
#
# ВНИМАНИЕ: build context у этого образа — КОРЕНЬ репозитория (см.
# docker-compose.yml), а не каталог pixelforge-frontend. Это нужно, чтобы
# в образ попал статический редактор Snap! из ./snap-source.

# --- build stage -----------------------------------------------------------
FROM node:22-alpine AS build
WORKDIR /app

COPY pixelforge-frontend/package.json pixelforge-frontend/package-lock.json ./
RUN npm ci

COPY pixelforge-frontend/ ./

# Значения VITE_* вшиваются в бандл на этапе сборки (см. compose args).
ARG VITE_API_BASE_URL
ARG VITE_API_ORIGIN
ARG VITE_SNAP_URL
ARG VITE_GDEVELOP_URL
RUN npm run build

# --- runtime stage -------------------------------------------------------
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
# Статический Snap! (без сборки) отдаётся тем же nginx по пути /snap/.
COPY snap-source /usr/share/nginx/html/snap
COPY pixelforge-frontend/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
