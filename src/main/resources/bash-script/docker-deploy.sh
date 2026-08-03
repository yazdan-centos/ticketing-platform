#!/usr/bin/env bash
# Unattended Docker deployment for AlmaLinux 10.
# Usage: sudo bash docker-deploy.sh
set -Eeuo pipefail

: "${APP_ROOT:=/opt/ticketing-docker}"
: "${SOURCE_REPOSITORY:=https://github.com/yazdan-centos/ticketing-platform.git}"
: "${SOURCE_BRANCH:=main}"
: "${FRONTEND_REPOSITORY:=https://github.com/yazdan-centos/collaboration2.git}"
: "${FRONTEND_BRANCH:=main}"
: "${REACT_APP_API_BASE_URL:=}"
: "${APP_PORT:=80}"
: "${SSH_PORT:=9011}"
: "${POSTGRES_DB:=ticketing_platform_db}"
: "${POSTGRES_USER:=ticketing}"
: "${POSTGRES_PASSWORD:=}"
: "${APP_JWT_SECRET:=}"
: "${APP_CORS_ALLOWED_ORIGIN_PATTERNS:=}"
: "${JAVA_TOOL_OPTIONS:=-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport}"

LOG_DIR=/var/log/ticketing-docker
ENV_DIR=/etc/ticketing-docker
LOG_FILE="$LOG_DIR/deploy-$(date +%Y%m%d-%H%M%S).log"
ENV_FILE="$ENV_DIR/app.env"

log() { printf '\n\033[1;34m==> %s\033[0m\n' "$*"; }
info() { printf '    %s\n' "$*"; }
warn() { printf '\033[1;33m[warn]\033[0m %s\n' "$*" >&2; }
die() { printf '\033[1;31m[fail]\033[0m %s\n' "$*" >&2; exit 1; }

on_error() {
  local exit_code=$? line_number=$1
  printf '\n\033[1;31m[fail]\033[0m deployment stopped at line %s (exit %s)\n' \
    "$line_number" "$exit_code" >&2
  if [[ -d "$APP_ROOT" ]] && command -v docker >/dev/null 2>&1; then
    if [[ -f "$ENV_FILE" ]]; then
      docker compose --env-file "$ENV_FILE" --project-directory "$APP_ROOT" logs --tail=100 2>/dev/null || true
    else
      docker compose --project-directory "$APP_ROOT" logs --tail=100 2>/dev/null || true
    fi
  fi
  printf 'Log: %s\n' "$LOG_FILE" >&2
  exit "$exit_code"
}
trap 'on_error $LINENO' ERR

[[ $EUID -eq 0 ]] || die "run this script as root"
[[ -f /etc/almalinux-release ]] || die "this installer requires AlmaLinux"
[[ "$SOURCE_REPOSITORY" == https://* ]] || die "SOURCE_REPOSITORY must use HTTPS"
[[ "$FRONTEND_REPOSITORY" == https://* ]] || die "FRONTEND_REPOSITORY must use HTTPS"
[[ "$SOURCE_BRANCH" =~ ^[A-Za-z0-9._/-]+$ ]] || die "SOURCE_BRANCH contains invalid characters"
[[ "$FRONTEND_BRANCH" =~ ^[A-Za-z0-9._/-]+$ ]] || die "FRONTEND_BRANCH contains invalid characters"
[[ "$POSTGRES_DB" =~ ^[a-z_][a-z0-9_]*$ ]] || die "POSTGRES_DB must be a lowercase identifier"
[[ "$POSTGRES_USER" =~ ^[a-z_][a-z0-9_]*$ ]] || die "POSTGRES_USER must be a lowercase identifier"
for port in "$APP_PORT" "$SSH_PORT"; do
  [[ "$port" =~ ^[0-9]+$ ]] || die "ports must be numeric"
  (( 10#$port >= 1 && 10#$port <= 65535 )) || die "ports must be between 1 and 65535"
done

mkdir -p "$LOG_DIR" "$ENV_DIR"
chmod 700 "$ENV_DIR"
exec > >(tee -a "$LOG_FILE") 2>&1
log "Docker deployment started at $(date -Is)"

log "Installing Docker CE and deployment tools"
dnf -y install ca-certificates curl firewalld git openssl >/dev/null
curl -fsSL https://download.docker.com/linux/centos/docker-ce.repo \
  -o /etc/yum.repos.d/docker-ce.repo
dnf -y install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
systemctl enable --now docker firewalld
docker version >/dev/null
docker compose version

log "Configuring firewall"
firewall-cmd --permanent --add-port="${SSH_PORT}/tcp" >/dev/null
firewall-cmd --permanent --add-port="${SSH_PORT}/udp" >/dev/null
firewall-cmd --permanent --add-port="${APP_PORT}/tcp" >/dev/null
firewall-cmd --reload >/dev/null
info "Allowed ${SSH_PORT}/tcp, ${SSH_PORT}/udp, and ${APP_PORT}/tcp"

log "Checking out the project over HTTPS"
if [[ -e "$APP_ROOT" && ! -d "$APP_ROOT/.git" ]]; then
  die "$APP_ROOT exists but is not a Git checkout"
fi
if [[ -d "$APP_ROOT/.git" ]]; then
  git -C "$APP_ROOT" remote set-url origin "$SOURCE_REPOSITORY"
  git -C "$APP_ROOT" fetch --prune origin "$SOURCE_BRANCH"
else
  mkdir -p "$(dirname "$APP_ROOT")"
  git clone --branch "$SOURCE_BRANCH" "$SOURCE_REPOSITORY" "$APP_ROOT"
fi
git -C "$APP_ROOT" checkout -B "$SOURCE_BRANCH" "origin/$SOURCE_BRANCH"
git -C "$APP_ROOT" reset --hard "origin/$SOURCE_BRANCH"
git -C "$APP_ROOT" clean -fd
info "Checked out $(git -C "$APP_ROOT" rev-parse --short HEAD) from $SOURCE_BRANCH"

read_env_value() {
  local key=$1
  [[ -f "$ENV_FILE" ]] || return 0
  sed -n "s/^${key}=//p" "$ENV_FILE" | tail -1
}

persisted_password=$(read_env_value POSTGRES_PASSWORD)
persisted_jwt_secret=$(read_env_value APP_JWT_SECRET)
persisted_cors=$(read_env_value APP_CORS_ALLOWED_ORIGIN_PATTERNS)
persisted_api_base_url=$(read_env_value REACT_APP_API_BASE_URL)

[[ -n "$POSTGRES_PASSWORD" ]] || POSTGRES_PASSWORD="$persisted_password"
[[ -n "$POSTGRES_PASSWORD" ]] || POSTGRES_PASSWORD=$(openssl rand -hex 24)
[[ -n "$APP_JWT_SECRET" ]] || APP_JWT_SECRET="$persisted_jwt_secret"
[[ -n "$APP_JWT_SECRET" ]] || APP_JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')

[[ -n "$REACT_APP_API_BASE_URL" ]] || REACT_APP_API_BASE_URL="$persisted_api_base_url"

public_ip=$(curl -4fsS --max-time 10 https://api.ipify.org || true)
if [[ -z "$REACT_APP_API_BASE_URL" ]]; then
  if [[ "$public_ip" =~ ^[0-9]{1,3}(\.[0-9]{1,3}){3}$ ]]; then
    if [[ "$APP_PORT" == 80 ]]; then
      REACT_APP_API_BASE_URL="http://${public_ip}"
    else
      REACT_APP_API_BASE_URL="http://${public_ip}:${APP_PORT}"
    fi
  else
    warn "public IP detection failed; using localhost for the React API URL"
    REACT_APP_API_BASE_URL="http://localhost:${APP_PORT}"
  fi
fi

if [[ -z "$APP_CORS_ALLOWED_ORIGIN_PATTERNS" ]]; then
  APP_CORS_ALLOWED_ORIGIN_PATTERNS="$persisted_cors"
fi
if [[ -z "$APP_CORS_ALLOWED_ORIGIN_PATTERNS" ]]; then
  if [[ "$public_ip" =~ ^[0-9]{1,3}(\.[0-9]{1,3}){3}$ ]]; then
    APP_CORS_ALLOWED_ORIGIN_PATTERNS="http://${public_ip}:*,http://localhost:*,http://127.0.0.1:*"
  else
    warn "public IP detection failed; using local CORS patterns"
    APP_CORS_ALLOWED_ORIGIN_PATTERNS="http://localhost:*,http://127.0.0.1:*"
  fi
fi

log "Writing persistent Compose configuration"
umask 077
cat > "$ENV_FILE" <<EOF
APP_PORT=${APP_PORT}
APP_IMAGE_TAG=latest
SOURCE_REPOSITORY=${SOURCE_REPOSITORY}
SOURCE_BRANCH=${SOURCE_BRANCH}
FRONTEND_REPOSITORY=${FRONTEND_REPOSITORY}
FRONTEND_BRANCH=${FRONTEND_BRANCH}
REACT_APP_API_BASE_URL=${REACT_APP_API_BASE_URL}
POSTGRES_DB=${POSTGRES_DB}
POSTGRES_USER=${POSTGRES_USER}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
APP_JWT_SECRET=${APP_JWT_SECRET}
APP_CORS_ALLOWED_ORIGIN_PATTERNS=${APP_CORS_ALLOWED_ORIGIN_PATTERNS}
JAVA_TOOL_OPTIONS=${JAVA_TOOL_OPTIONS}
EOF
chmod 600 "$ENV_FILE"

[[ -f "$APP_ROOT/Dockerfile" ]] || die "Dockerfile is missing from the GitHub checkout"
[[ -f "$APP_ROOT/docker-compose.yml" ]] || die "docker-compose.yml is missing from the GitHub checkout"

log "Validating and building the Compose stack"
docker compose --env-file "$ENV_FILE" --project-directory "$APP_ROOT" config --quiet
docker compose --env-file "$ENV_FILE" --project-directory "$APP_ROOT" pull postgres
docker compose --env-file "$ENV_FILE" --project-directory "$APP_ROOT" build --no-cache --pull app frontend

log "Starting the application"
docker compose --env-file "$ENV_FILE" --project-directory "$APP_ROOT" up -d --remove-orphans

log "Waiting for HTTP readiness"
ready=0
for attempt in $(seq 1 60); do
  if curl -fsS --max-time 5 "http://127.0.0.1:${APP_PORT}/" >/dev/null \
     && curl -fsS --max-time 5 "http://127.0.0.1:${APP_PORT}/v3/api-docs" >/dev/null; then
    ready=1
    break
  fi
  info "Waiting for application (${attempt}/60)"
  sleep 5
done

if (( ready == 0 )); then
  docker compose --env-file "$ENV_FILE" --project-directory "$APP_ROOT" ps
  docker compose --env-file "$ENV_FILE" --project-directory "$APP_ROOT" logs --tail=150 app frontend
  die "application did not become ready on port $APP_PORT"
fi

log "Deployment completed successfully"
docker compose --env-file "$ENV_FILE" --project-directory "$APP_ROOT" ps
info "Application: http://$(hostname -I | awk '{print $1}'):${APP_PORT}"
info "Swagger UI: http://$(hostname -I | awk '{print $1}'):${APP_PORT}/swagger-ui/index.html"
info "Project: $APP_ROOT"
info "Environment: $ENV_FILE"
info "Log: $LOG_FILE"
