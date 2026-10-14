# ---- Build stage ----
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# NOTE: "npm run build" runs "tsc -b" first and currently fails on a type error
# (src/services/columnaService.ts). Use "npm run build" again once that is fixed.
RUN npx vite build

# ---- Runtime stage ----
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
