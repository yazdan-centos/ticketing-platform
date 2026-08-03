#!/usr/bin/env bash
#===============================================================================
# Unattended deployment: Ticketing platform (React frontend + Spring Boot API)
# Target: AlmaLinux 10.0 / IP-only host (155.117.13.33)
# Usage : sudo bash ./deploy.sh
# Re-run safe (idempotent).
#===============================================================================
set -Eeuo pipefail

#------------------------------- CONFIGURATION ---------------------------------
: "${APP_USER:=ticketing}"
: "${APP_ROOT:=/opt/ticketing}"
: "${ENV_DIR:=/etc/ticketing}"
: "${LOG_DIR:=/var/log/ticketing}"

# Repository contents are opposite the labels supplied in the original request:
# collaboration2 is React, while ticketing-platform is Spring Boot.
: "${FRONTEND_REPO:=https://github.com/yazdan-centos/collaboration2.git}"
: "${BACKEND_REPO:=https://github.com/yazdan-centos/ticketing-platform.git}"
: "${FRONTEND_BRANCH:=}"          # empty = remote default branch
: "${BACKEND_BRANCH:=}"

: "${DB_NAME:=${POSTGRES_DB:-ticketing}}"
: "${DB_USER:=${POSTGRES_USER:-ticketing}}"
: "${DB_HOST:=127.0.0.1}"
: "${DB_PORT:=5432}"
: "${DB_PASS:=${POSTGRES_PASSWORD:-}}" # empty = generate once and persist

: "${BACKEND_PORT:=8080}"
: "${BACKEND_BIND:=127.0.0.1}"
: "${FRONTEND_PORT:=3000}"
: "${HTTP_PORT:=80}"
: "${SSH_PORT:=9011}"
: "${PUBLIC_IP:=155.117.13.33}"

: "${JAVA_PKG:=java-21-openjdk-devel}"
: "${NODE_MAJOR:=22}"
: "${JPA_DDL_AUTO:=validate}"     # Flyway owns the production schema
: "${SPRING_PROFILE:=prod}"
: "${NODE_BUILD_MEM:=2048}"
: "${APP_JWT_SECRET:=}"           # empty = generate once and persist
: "${MAX_UPLOAD_SIZE:=25MB}"
: "${NGINX_MAX_UPLOAD_SIZE:=25m}"

BACKEND_DIR="$APP_ROOT/backend"
FRONTEND_DIR="$APP_ROOT/frontend"
FRONTEND_RELEASE_ROOT="$APP_ROOT/releases/frontend"
FRONTEND_CURRENT="$APP_ROOT/frontend-current"
DATA_DIR="/var/lib/ticketing"
UPLOAD_DIR="$DATA_DIR/uploads"
BACKEND_SERVICE=ticketing-backend
FRONTEND_SERVICE=ticketing-frontend
DEPLOY_LOG="$LOG_DIR/deploy-$(date +%Y%m%d-%H%M%S).log"
if [[ "$HTTP_PORT" == 80 ]]; then
  PUBLIC_ORIGIN="http://${PUBLIC_IP}"
else
  PUBLIC_ORIGIN="http://${PUBLIC_IP}:${HTTP_PORT}"
fi

#--------------------------------- HELPERS -------------------------------------
log()  { printf '\n\033[1;34m==> %s\033[0m\n' "$*"; }
info() { printf '    %s\n' "$*"; }
warn() { printf '\033[1;33m[warn]\033[0m %s\n' "$*" >&2; }
die()  { printf '\033[1;31m[fail]\033[0m %s\n' "$*" >&2; exit 1; }

write_file() {
  local target="$1" mode="$2" owner="$3" group="$4"
  local temporary="${target}.new"
  install -o "$owner" -g "$group" -m "$mode" /dev/null "$temporary"
  cat > "$temporary"
  mv -f "$temporary" "$target"
}

on_error() {
  local rc=$? line=$1
  printf '\n\033[1;31m[fail]\033[0m aborted at line %s (exit %s). Log: %s\n' "$line" "$rc" "$DEPLOY_LOG" >&2
  exit "$rc"
}
trap 'on_error $LINENO' ERR

[[ $EUID -eq 0 ]] || die "must run as root"
[[ -f /etc/almalinux-release || -f /etc/redhat-release ]] || warn "not an EL-family host; continuing anyway"
[[ "$APP_USER" =~ ^[a-z_][a-z0-9_-]*\$?$ ]] || die "APP_USER is not a valid service account name"
[[ "$DB_NAME" =~ ^[a-z_][a-z0-9_]*$ ]] || die "DB_NAME must be a lowercase PostgreSQL identifier"
[[ "$DB_USER" =~ ^[a-z_][a-z0-9_]*$ ]] || die "DB_USER must be a lowercase PostgreSQL identifier"
[[ "$DB_HOST" =~ ^[A-Za-z0-9._-]+$ ]] || die "DB_HOST contains invalid characters"
[[ "$PUBLIC_IP" =~ ^[0-9]{1,3}(\.[0-9]{1,3}){3}$ ]] || die "PUBLIC_IP must be an IPv4 address"
[[ "$MAX_UPLOAD_SIZE" =~ ^[0-9]+(KB|MB|GB)$ ]] || die "MAX_UPLOAD_SIZE must look like 25MB"
[[ "$NGINX_MAX_UPLOAD_SIZE" =~ ^[0-9]+[kKmMgG]$ ]] || die "NGINX_MAX_UPLOAD_SIZE must look like 25m"
[[ "$DB_PORT" =~ ^[0-9]+$ && "$BACKEND_PORT" =~ ^[0-9]+$ && "$FRONTEND_PORT" =~ ^[0-9]+$ \
   && "$HTTP_PORT" =~ ^[0-9]+$ && "$SSH_PORT" =~ ^[0-9]+$ ]] \
  || die "all configured ports must be numeric"
for port in "$DB_PORT" "$BACKEND_PORT" "$FRONTEND_PORT" "$HTTP_PORT" "$SSH_PORT"; do
  (( 10#$port >= 1 && 10#$port <= 65535 )) || die "all configured ports must be between 1 and 65535"
done

mkdir -p "$LOG_DIR" "$ENV_DIR" "$APP_ROOT"
chmod 750 "$ENV_DIR"
exec > >(tee -a "$DEPLOY_LOG") 2>&1
log "Deployment started $(date -Is) — log: $DEPLOY_LOG"

#--------------------------- 1. SYSTEM DEPENDENCIES ----------------------------
log "Installing base packages"
dnf -y install dnf-plugins-core >/dev/null
dnf -y install \
  git curl wget tar unzip which policycoreutils-python-utils \
  nginx postgresql-server postgresql-contrib \
  "$JAVA_PKG" firewalld openssl rsync procps-ng

node_major() {
  command -v node >/dev/null 2>&1 || { echo 0; return; }
  local version
  if ! version=$(node -v 2>/dev/null); then
    echo 0
    return
  fi
  sed 's/^v\([0-9]*\).*/\1/' <<< "$version"
}

install_nodejs() {
  local current
  current=$(node_major)
  if (( current == 0 )) && command -v node >/dev/null 2>&1; then
    warn "installed Node.js is broken; synchronizing c-ares and libuv"
    dnf -y upgrade c-ares libuv --disablerepo='nodesource*'
    current=$(node_major)
  fi
  if (( current >= 18 )) && command -v npm >/dev/null 2>&1; then
    info "node $(node -v) / npm $(npm -v) already present"
    return
  fi

  info "installing Node.js from AlmaLinux repositories"
  if dnf -y install nodejs npm c-ares libuv --disablerepo='nodesource*' >/dev/null 2>&1 \
     && (( $(node_major) >= 18 )) && command -v npm >/dev/null 2>&1; then
    info "node $(node -v) / npm $(npm -v)"
    return
  fi

  warn "distro Node.js unavailable; falling back to NodeSource ${NODE_MAJOR}.x"
  dnf -y remove nodejs-full-i18n nodejs-docs npm nodejs >/dev/null 2>&1 || true
  curl -fsSL "https://rpm.nodesource.com/setup_${NODE_MAJOR}.x" | bash -
  dnf -y install nodejs --allowerasing
  (( $(node_major) >= 18 )) && command -v npm >/dev/null 2>&1 \
    || die "Node.js installation failed"
  info "node $(node -v) / npm $(npm -v)"
}
log "Ensuring Node.js toolchain"
install_nodejs

log "Ensuring static file server for the frontend"
npm ls -g --depth=0 serve >/dev/null 2>&1 || npm install -g serve@14 --no-fund --no-audit

#------------------------------ 2. SERVICE USER --------------------------------
log "Ensuring service account: $APP_USER"
id -u "$APP_USER" >/dev/null 2>&1 || \
  useradd --system --create-home --home-dir "/home/$APP_USER" --shell /sbin/nologin "$APP_USER"
mkdir -p "$BACKEND_DIR" "$FRONTEND_DIR" "$LOG_DIR"
install -d -o "$APP_USER" -g "$APP_USER" -m 0750 \
  "$DATA_DIR" "$UPLOAD_DIR" "$APP_ROOT/releases" "$FRONTEND_RELEASE_ROOT"
chown -R "$APP_USER:$APP_USER" "$APP_ROOT" "$LOG_DIR"

#------------------------------ 3. FIREWALL / SELINUX --------------------------
log "Configuring firewalld (SSH ${SSH_PORT}, HTTP ${HTTP_PORT})"
if systemctl is-active --quiet firewalld; then
  mapfile -t FIREWALL_ZONES < <(firewall-cmd --get-active-zones | awk '/^[^[:space:]]/{print $1}')
  if (( ${#FIREWALL_ZONES[@]} == 0 )); then
    FIREWALL_ZONES=("$(firewall-cmd --get-default-zone)")
  fi
  for FIREWALL_ZONE in "${FIREWALL_ZONES[@]}"; do
    firewall-cmd --permanent --zone="$FIREWALL_ZONE" --add-port="${SSH_PORT}/tcp" >/dev/null
    firewall-cmd --permanent --zone="$FIREWALL_ZONE" --add-port="${SSH_PORT}/udp" >/dev/null
    firewall-cmd --permanent --zone="$FIREWALL_ZONE" --add-port="${HTTP_PORT}/tcp" >/dev/null
  done
  firewall-cmd --reload >/dev/null
else
  FIREWALL_ZONE=$(firewall-offline-cmd --get-default-zone)
  firewall-offline-cmd --zone="$FIREWALL_ZONE" --add-port="${SSH_PORT}/tcp" >/dev/null
  firewall-offline-cmd --zone="$FIREWALL_ZONE" --add-port="${SSH_PORT}/udp" >/dev/null
  firewall-offline-cmd --zone="$FIREWALL_ZONE" --add-port="${HTTP_PORT}/tcp" >/dev/null
  systemctl enable --now firewalld >/dev/null
fi
systemctl enable firewalld >/dev/null
info "app ports ${BACKEND_PORT}/${FRONTEND_PORT} intentionally NOT exposed (loopback only)"

if command -v getenforce >/dev/null && [[ $(getenforce) != Disabled ]]; then
  log "Allowing Nginx to proxy to loopback (SELinux)"
  setsebool -P httpd_can_network_connect 1
fi

#------------------------------ 4. POSTGRESQL ----------------------------------
log "Initialising PostgreSQL"
PGDATA=$(systemctl show -p Environment postgresql.service | sed -n 's/.*PGDATA=\([^ ]*\).*/\1/p')
PGDATA="${PGDATA:-/var/lib/pgsql/data}"
if [[ ! -f "$PGDATA/PG_VERSION" ]]; then
  /usr/bin/postgresql-setup --initdb
else
  info "cluster already initialised at $PGDATA"
fi

PG_HBA="$PGDATA/pg_hba.conf"
cp -n "$PG_HBA" "${PG_HBA}.orig"
sed -ri 's@^(host[[:space:]]+all[[:space:]]+all[[:space:]]+127\.0\.0\.1/32[[:space:]]+)(ident|peer|trust|md5|password|scram-sha-256)@\1md5@' "$PG_HBA"
sed -ri 's@^(host[[:space:]]+all[[:space:]]+all[[:space:]]+::1/128[[:space:]]+)(ident|peer|trust|md5|password|scram-sha-256)@\1md5@' "$PG_HBA"
grep -qE '^password_encryption' "$PGDATA/postgresql.conf" \
  && sed -ri "s@^#?password_encryption.*@password_encryption = md5@" "$PGDATA/postgresql.conf" \
  || echo "password_encryption = md5" >> "$PGDATA/postgresql.conf"
if grep -qE '^[[:space:]]*#?[[:space:]]*listen_addresses[[:space:]]*=' "$PGDATA/postgresql.conf"; then
  sed -ri "0,/^[[:space:]]*#?[[:space:]]*listen_addresses[[:space:]]*=.*$/s//listen_addresses = '127.0.0.1,::1'/" \
    "$PGDATA/postgresql.conf"
else
  echo "listen_addresses = '127.0.0.1,::1'" >> "$PGDATA/postgresql.conf"
fi

systemctl enable postgresql >/dev/null
systemctl restart postgresql
for _ in {1..30}; do
  runuser -u postgres -- pg_isready -q -h "$DB_HOST" -p "$DB_PORT" && break
  sleep 1
done
runuser -u postgres -- pg_isready -q -h "$DB_HOST" -p "$DB_PORT" \
  || die "PostgreSQL did not become ready on ${DB_HOST}:${DB_PORT}"

# Password: reuse the persisted one so re-runs never break existing config.
if [[ -z "$DB_PASS" && -f "$ENV_DIR/db.env" ]]; then
  DB_PASS=$(grep -E '^DB_PASS=' "$ENV_DIR/db.env" | cut -d= -f2- || true)
fi
[[ -n "$DB_PASS" ]] || DB_PASS=$(openssl rand -base64 30 | tr -d '/+=' | cut -c1-28)
[[ "$DB_PASS" =~ ^[A-Za-z0-9._~!@%^+=,-]+$ ]] \
  || die "DB_PASS may contain only letters, digits, and ._~!@%^+=,-"

psql_admin() { runuser -u postgres -- psql -v ON_ERROR_STOP=1 -qtAX -c "$1"; }
log "Ensuring PostgreSQL login role '$DB_USER' and database '$DB_NAME'"
if [[ $(psql_admin "SELECT 1 FROM pg_roles WHERE rolname='${DB_USER}'") != 1 ]]; then
  psql_admin "CREATE ROLE ${DB_USER} LOGIN PASSWORD '${DB_PASS}'" >/dev/null
else
  psql_admin "ALTER ROLE ${DB_USER} WITH LOGIN PASSWORD '${DB_PASS}'" >/dev/null
fi
[[ $(psql_admin "SELECT left(rolpassword, 3) FROM pg_authid WHERE rolname='${DB_USER}'") == md5 ]] \
  || die "PostgreSQL role '$DB_USER' was not stored with MD5 password encryption"
if [[ $(psql_admin "SELECT 1 FROM pg_database WHERE datname='${DB_NAME}'") != 1 ]]; then
  runuser -u postgres -- createdb -O "$DB_USER" -E UTF8 "$DB_NAME"
fi
psql_admin "GRANT ALL PRIVILEGES ON DATABASE ${DB_NAME} TO ${DB_USER}" >/dev/null
runuser -u postgres -- psql -v ON_ERROR_STOP=1 -qtAX -d "$DB_NAME" \
  -c "GRANT ALL ON SCHEMA public TO ${DB_USER}" >/dev/null

write_file "$ENV_DIR/db.env" 0600 root root <<EOF
DB_NAME=${DB_NAME}
DB_USER=${DB_USER}
DB_PASS=${DB_PASS}
DB_HOST=${DB_HOST}
DB_PORT=${DB_PORT}
EOF

#--------------------------- 5. SOURCE CHECKOUT --------------------------------
clone_or_update() {
  local repo="$1" dir="$2" branch="$3"
  if [[ -d "$dir/.git" ]] \
     && [[ $(runuser -u "$APP_USER" -- git -C "$dir" remote get-url origin) != "$repo" ]]; then
    warn "repository URL changed for $dir; replacing the old checkout"
    rm -rf "$dir"
  fi
  if [[ -d "$dir/.git" ]]; then
    info "updating $(basename "$dir")"
    runuser -u "$APP_USER" -- git -C "$dir" remote set-url origin "$repo"
    runuser -u "$APP_USER" -- git -C "$dir" fetch --prune --tags origin
  else
    info "cloning $repo"
    rm -rf "$dir"; mkdir -p "$dir"; chown "$APP_USER:$APP_USER" "$dir"
    runuser -u "$APP_USER" -- git clone --quiet "$repo" "$dir"
  fi
  if [[ -z "$branch" ]]; then
    branch=$(runuser -u "$APP_USER" -- git -C "$dir" remote show origin \
             | sed -n 's/.*HEAD branch: //p' | head -1)
    branch="${branch:-main}"
  fi
  runuser -u "$APP_USER" -- git -C "$dir" checkout -q -B "$branch" "origin/$branch"
  runuser -u "$APP_USER" -- git -C "$dir" reset -q --hard "origin/$branch"
  runuser -u "$APP_USER" -- git -C "$dir" clean -qfd -e node_modules -e target -e build/libs
  info "$(basename "$dir") @ $branch $(runuser -u "$APP_USER" -- git -C "$dir" rev-parse --short HEAD)"
}

log "Fetching backend sources"
clone_or_update "$BACKEND_REPO" "$BACKEND_DIR" "$BACKEND_BRANCH"
log "Fetching frontend sources"
clone_or_update "$FRONTEND_REPO" "$FRONTEND_DIR" "$FRONTEND_BRANCH"

[[ -f "$BACKEND_DIR/pom.xml" || -f "$BACKEND_DIR/gradlew" ]] \
  || die "BACKEND_REPO is not a Maven/Gradle application: $BACKEND_REPO"
[[ -f "$FRONTEND_DIR/package.json" ]] \
  || die "FRONTEND_REPO is not a Node application: $FRONTEND_REPO"

# Git on Linux is case-sensitive. Keep deployment compatible with the current
# repository, which tracks Logo.png while Sidebar.jsx still imports LOGO.png.
if [[ -f "$FRONTEND_DIR/src/assets/img/Logo.png" \
   && -f "$FRONTEND_DIR/src/components/Sidebar.jsx" ]]; then
  sed -i "s@../assets/img/LOGO\\.png@../assets/img/Logo.png@g" \
    "$FRONTEND_DIR/src/components/Sidebar.jsx"
fi

#--------------------------- 6. BACKEND BUILD ----------------------------------
log "Building Spring Boot backend"
JAVA_HOME=$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")")
export JAVA_HOME
info "JAVA_HOME=$JAVA_HOME"

build_backend() {
  cd "$BACKEND_DIR"
  if [[ -f ./mvnw ]]; then
    chmod +x ./mvnw
    runuser -u "$APP_USER" -- env HOME="/home/$APP_USER" JAVA_HOME="$JAVA_HOME" \
      ./mvnw -B -q -DskipTests clean package
  elif [[ -f pom.xml ]]; then
    command -v mvn >/dev/null || dnf -y install maven
    runuser -u "$APP_USER" -- env HOME="/home/$APP_USER" JAVA_HOME="$JAVA_HOME" \
      mvn -B -q -DskipTests clean package
  elif [[ -f ./gradlew ]]; then
    chmod +x ./gradlew
    runuser -u "$APP_USER" -- env HOME="/home/$APP_USER" JAVA_HOME="$JAVA_HOME" \
      ./gradlew --no-daemon -q clean bootJar -x test
  else
    die "no Maven or Gradle build found in $BACKEND_DIR"
  fi
}
build_backend

BACKEND_JAR=$(find "$BACKEND_DIR" \( -path '*/target/*.jar' -o -path '*/build/libs/*.jar' \) \
  ! -name '*-plain.jar' ! -name '*sources*' ! -name '*javadoc*' -printf '%T@ %p\n' \
  | sort -rn | head -1 | cut -d' ' -f2-)
[[ -n "$BACKEND_JAR" ]] || die "build produced no runnable jar"
install -o "$APP_USER" -g "$APP_USER" -m 0644 "$BACKEND_JAR" "$APP_ROOT/backend-app.jar.new"
mv -f "$APP_ROOT/backend-app.jar.new" "$APP_ROOT/backend-app.jar"
info "artifact: $(basename "$BACKEND_JAR")"

if [[ -z "$APP_JWT_SECRET" && -f "$ENV_DIR/backend.env" ]]; then
  APP_JWT_SECRET=$(grep -E '^APP_JWT_SECRET=' "$ENV_DIR/backend.env" | cut -d= -f2- || true)
fi
[[ -n "$APP_JWT_SECRET" ]] || APP_JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')
[[ "$APP_JWT_SECRET" =~ ^[A-Za-z0-9+/=]+$ ]] || die "APP_JWT_SECRET must be standard Base64"

write_file "$ENV_DIR/backend.env" 0640 root "$APP_USER" <<EOF
SPRING_PROFILES_ACTIVE=${SPRING_PROFILE}
SERVER_PORT=${BACKEND_PORT}
SERVER_ADDRESS=${BACKEND_BIND}
SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
SPRING_DATASOURCE_USERNAME=${DB_USER}
SPRING_DATASOURCE_PASSWORD=${DB_PASS}
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
SPRING_JPA_HIBERNATE_DDL_AUTO=${JPA_DDL_AUTO}
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=${MAX_UPLOAD_SIZE}
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=${MAX_UPLOAD_SIZE}
APP_JWT_SECRET=${APP_JWT_SECRET}
APP_CORS_ALLOWED_ORIGIN_PATTERNS=${PUBLIC_ORIGIN}
FILE_UPLOAD_DIR=${UPLOAD_DIR}
FLYWAY_ENABLED=true
EOF

write_file "/etc/systemd/system/${BACKEND_SERVICE}.service" 0644 root root <<EOF
[Unit]
Description=Ticketing Backend (Spring Boot)
After=network-online.target postgresql.service
Wants=network-online.target
Requires=postgresql.service

[Service]
Type=simple
User=${APP_USER}
Group=${APP_USER}
WorkingDirectory=${APP_ROOT}
EnvironmentFile=${ENV_DIR}/backend.env
ExecStart=${JAVA_HOME}/bin/java -XX:+UseSerialGC -Xms256m -Xmx768m -jar ${APP_ROOT}/backend-app.jar
Restart=always
RestartSec=5
SuccessExitStatus=143
StandardOutput=append:${LOG_DIR}/backend.log
StandardError=append:${LOG_DIR}/backend.log
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=full
ProtectHome=true
ReadWritePaths=${DATA_DIR}
UMask=0027

[Install]
WantedBy=multi-user.target
EOF

#--------------------------- 7. FRONTEND BUILD ---------------------------------
log "Building React frontend"
cd "$FRONTEND_DIR"
write_file "$ENV_DIR/frontend.env" 0640 root "$APP_USER" <<EOF
NODE_ENV=production
PORT=${FRONTEND_PORT}
HOST=127.0.0.1
EOF

# Frontend request paths already begin with /api, so the base must be the origin.
write_file "$FRONTEND_DIR/.env.production" 0644 "$APP_USER" "$APP_USER" <<EOF
REACT_APP_API_BASE_URL=${PUBLIC_ORIGIN}
VITE_API_BASE_URL=${PUBLIC_ORIGIN}
EOF

npm_run() { runuser -u "$APP_USER" -- env HOME="/home/$APP_USER" \
  NODE_OPTIONS="--max-old-space-size=${NODE_BUILD_MEM}" CI=false npm "$@"; }

if [[ -f package-lock.json ]]; then
  npm_run ci --no-audit --no-fund || npm_run install --legacy-peer-deps --no-audit --no-fund
else
  npm_run install --legacy-peer-deps --no-audit --no-fund
fi

npm_run run build

FRONTEND_BUILD_DIR=""
for d in build dist out; do
  [[ -d "$FRONTEND_DIR/$d" ]] && { FRONTEND_BUILD_DIR="$FRONTEND_DIR/$d"; break; }
done

[[ -n "$FRONTEND_BUILD_DIR" ]] || die "frontend build produced no build, dist, or out directory"
FRONTEND_REVISION=$(runuser -u "$APP_USER" -- git -C "$FRONTEND_DIR" rev-parse --short=12 HEAD)
FRONTEND_RELEASE_DIR="${FRONTEND_RELEASE_ROOT}/${FRONTEND_REVISION}-$(date +%Y%m%d%H%M%S)"
install -d -o "$APP_USER" -g "$APP_USER" -m 0750 "$FRONTEND_RELEASE_DIR"
rsync -a --delete "$FRONTEND_BUILD_DIR/" "$FRONTEND_RELEASE_DIR/"
chown -R "$APP_USER:$APP_USER" "$FRONTEND_RELEASE_DIR"
rm -f "${FRONTEND_CURRENT}.new"
ln -s "$FRONTEND_RELEASE_DIR" "${FRONTEND_CURRENT}.new"
mv -Tf "${FRONTEND_CURRENT}.new" "$FRONTEND_CURRENT"
FRONTEND_EXEC="$(command -v serve) -s ${FRONTEND_CURRENT} -l tcp://127.0.0.1:${FRONTEND_PORT}"
info "frontend release: ${FRONTEND_RELEASE_DIR}"

write_file "/etc/systemd/system/${FRONTEND_SERVICE}.service" 0644 root root <<EOF
[Unit]
Description=Ticketing Frontend (Node)
After=network-online.target ${BACKEND_SERVICE}.service
Wants=network-online.target

[Service]
Type=simple
User=${APP_USER}
Group=${APP_USER}
WorkingDirectory=${FRONTEND_CURRENT}
Environment=HOME=/home/${APP_USER}
EnvironmentFile=${ENV_DIR}/frontend.env
ExecStart=${FRONTEND_EXEC}
Restart=always
RestartSec=5
StandardOutput=append:${LOG_DIR}/frontend.log
StandardError=append:${LOG_DIR}/frontend.log
NoNewPrivileges=true
PrivateTmp=true

[Install]
WantedBy=multi-user.target
EOF

#----------------------------- 8. NGINX ----------------------------------------
log "Configuring Nginx reverse proxy"
rm -f /etc/nginx/conf.d/00-websocket-map.conf

write_file /etc/nginx/conf.d/ticketing.conf 0644 root root <<EOF
map \$http_upgrade \$connection_upgrade { default upgrade; '' close; }

upstream ticketing_api { server 127.0.0.1:${BACKEND_PORT}; keepalive 16; }
upstream ticketing_web { server 127.0.0.1:${FRONTEND_PORT}; keepalive 16; }

server {
    listen ${HTTP_PORT};
    listen [::]:${HTTP_PORT};
    server_name ${PUBLIC_IP};

    client_max_body_size ${NGINX_MAX_UPLOAD_SIZE};
    add_header X-Content-Type-Options nosniff always;
    add_header X-Frame-Options SAMEORIGIN always;
    add_header Referrer-Policy strict-origin-when-cross-origin always;

    proxy_http_version 1.1;
    proxy_set_header Host              \$host;
    proxy_set_header X-Real-IP         \$remote_addr;
    proxy_set_header X-Forwarded-For   \$proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto \$scheme;
    proxy_set_header Upgrade           \$http_upgrade;
    proxy_set_header Connection        \$connection_upgrade;
    proxy_read_timeout 300s;
    proxy_send_timeout 300s;

    location /api/ { proxy_pass http://ticketing_api; }
    location /uploads/ { proxy_pass http://ticketing_api; }
    location /v3/api-docs { proxy_pass http://ticketing_api; }
    location /swagger-ui/ { proxy_pass http://ticketing_api; }

    location /actuator/health {
        proxy_pass http://ticketing_api/actuator/health;
        access_log off;
    }

    location / { proxy_pass http://ticketing_web; }
}
EOF

nginx -t
systemctl enable nginx >/dev/null

#--------------------------- 9. START EVERYTHING -------------------------------
log "Starting services"
systemctl daemon-reload
systemctl enable "${BACKEND_SERVICE}" "${FRONTEND_SERVICE}" >/dev/null
systemctl restart "${BACKEND_SERVICE}"
systemctl restart "${FRONTEND_SERVICE}"
systemctl reload-or-restart nginx

wait_for_port() {
  local port="$1" label="$2" tries="${3:-90}"
  for ((i=0; i<tries; i++)); do
    (exec 3<>"/dev/tcp/127.0.0.1/${port}") 2>/dev/null && { exec 3<&-; info "$label up on :$port"; return 0; }
    sleep 2
  done
  warn "$label did not answer on :$port within $((tries*2))s"
  return 1
}

BACKEND_OK=0; FRONTEND_OK=0
wait_for_port "$BACKEND_PORT"  "backend"  120 && BACKEND_OK=1
wait_for_port "$FRONTEND_PORT" "frontend" 60  && FRONTEND_OK=1
HTTP_CODE=$(curl -s -H "Host: ${PUBLIC_IP}" -o /dev/null -w '%{http_code}' \
  "http://127.0.0.1:${HTTP_PORT}/" || true)
HTTP_CODE="${HTTP_CODE:-000}"
systemctl is-active --quiet "$BACKEND_SERVICE" || BACKEND_OK=0
systemctl is-active --quiet "$FRONTEND_SERVICE" || FRONTEND_OK=0
NGINX_OK=0
if [[ "$HTTP_CODE" =~ ^(2|3)[0-9][0-9]$ ]] && systemctl is-active --quiet nginx; then
  NGINX_OK=1
else
  warn "Nginx returned HTTP ${HTTP_CODE}"
fi

#------------------------------- 10. SUMMARY -----------------------------------
cat <<EOF

$(printf '=%.0s' {1..72})
 DEPLOYMENT SUMMARY
$(printf '=%.0s' {1..72})
 Application URL : ${PUBLIC_ORIGIN}/
 API base path   : ${PUBLIC_ORIGIN}/api
 Frontend        : 127.0.0.1:${FRONTEND_PORT}  [$([[ $FRONTEND_OK == 1 ]] && echo UP || echo CHECK-LOGS)]
 Backend         : ${BACKEND_BIND}:${BACKEND_PORT}  [$([[ $BACKEND_OK == 1 ]] && echo UP || echo CHECK-LOGS)]
 Nginx / status  : ${HTTP_CODE}  [$([[ $NGINX_OK == 1 ]] && echo UP || echo CHECK-LOGS)]
 Database        : ${DB_NAME} @ ${DB_HOST}:${DB_PORT} (user ${DB_USER})
 Database auth   : md5 (loopback only)
 Secrets         : ${ENV_DIR}/db.env (600), ${ENV_DIR}/backend.env (640)
 Logs            : ${LOG_DIR}/backend.log, ${LOG_DIR}/frontend.log
 Deploy log      : ${DEPLOY_LOG}

 Manage:
   systemctl status ${BACKEND_SERVICE} ${FRONTEND_SERVICE} nginx
   journalctl -u ${BACKEND_SERVICE} -f
   tail -f ${LOG_DIR}/backend.log

 Re-deploy latest code: sudo bash $0
$(printf '=%.0s' {1..72})
EOF

if (( BACKEND_OK == 0 || FRONTEND_OK == 0 || NGINX_OK == 0 )); then
  warn "one or more services are not listening yet — inspect the logs above"
  exit 1
fi
log "Deployment finished successfully"
