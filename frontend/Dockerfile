FROM node:22.23.4-alpine AS build
RUN apk add --no-cache --upgrade ca-certificates
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build
FROM nginx:1.29-alpine
RUN apk add --no-cache --upgrade ca-certificates
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
