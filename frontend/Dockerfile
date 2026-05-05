# Multi-stage build for React frontend with Node.js and Vite
FROM node:22-alpine AS build
WORKDIR /app
COPY app/package*.json ./
RUN npm ci
COPY app/ .
RUN npm run build

FROM nginx:alpine
# Install envsubst
RUN apk add --no-cache gettext
# Substitute environment variables in nginx.conf
COPY app/nginx.conf /tmp/nginx.conf
RUN envsubst '${BACKEND_URL}' < /tmp/nginx.conf > /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
CMD ["nginx", "-g", "daemon off;"]