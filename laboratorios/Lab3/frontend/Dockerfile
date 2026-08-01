# ============================================
# Dockerfile para frontend React + Vite (Lab2)
# ============================================

# Etapa 1: Build con Node
FROM node:20-alpine AS build

WORKDIR /app

# Copiar dependencias y instalarlas
COPY package.json package-lock.json ./
RUN npm ci

# Copiar código fuente y compilar
COPY public ./public
COPY src ./src
COPY index.html vite.config.js ./
RUN npm run build

# Etapa 2: Servir con Nginx
FROM nginx:alpine

# Copiar los archivos compilados
COPY --from=build /app/dist /usr/share/nginx/html

# Copiar config personalizada (opcional)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
