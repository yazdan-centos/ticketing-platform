FROM node:22-alpine AS frontend-build

ARG FRONTEND_REPOSITORY=https://github.com/yazdan-centos/collaboration2.git
ARG FRONTEND_BRANCH=main
ARG REACT_APP_API_BASE_URL=http://localhost

RUN apk add --no-cache ca-certificates git

WORKDIR /workspace

RUN case "$FRONTEND_REPOSITORY" in https://*) ;; *) echo "FRONTEND_REPOSITORY must use HTTPS" >&2; exit 1 ;; esac \
    && git clone --depth 1 --branch "$FRONTEND_BRANCH" "$FRONTEND_REPOSITORY" source

WORKDIR /workspace/source

ENV CI=true
ENV REACT_APP_API_BASE_URL=$REACT_APP_API_BASE_URL

RUN npm ci --no-audit --no-fund \
    && npm run build

FROM nginx:1.25-alpine

COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=frontend-build /workspace/source/build/ /usr/share/nginx/html/
