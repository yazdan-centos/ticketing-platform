#!/usr/bin/env bash
# -----------------------------------------------------------------------------
# Squid Proxy Installer + Basic Authentication Setup for Ubuntu 22.04 x86_64
#
# What this script does:
#   1) Installs Squid and required tools
#   2) Creates a proxy authentication user:
#        Username: cent
#        Password: sgsec@1390
#   3) Configures Squid to require authentication
#   4) Opens firewall port 3128 if UFW is active
#   5) Enables and starts the Squid service
#
# Usage:
#   chmod +x setup-squid-ubuntu.sh
#   sudo ./setup-squid-ubuntu.sh
# -----------------------------------------------------------------------------

set -euo pipefail

# -----------------------------
# Variables
# -----------------------------
SQUID_PORT="3128"
SQUID_CONF="/etc/squid/squid.conf"
SQUID_PASSWD_FILE="/etc/squid/passwd"

AUTH_USER="cent"
AUTH_PASS='sgsec@1390'

# -----------------------------
# Helper functions
# -----------------------------
log()  { echo "[+] $*"; }
warn() { echo "[!] $*" >&2; }
die()  { echo "[x] $*" >&2; exit 1; }

need_root() {
  if [[ "${EUID}" -ne 0 ]]; then
    die "This script must be run as root. Try: sudo $0"
  fi
}

cmd_exists() {
  command -v "$1" >/dev/null 2>&1
}

# -----------------------------
# Main
# -----------------------------
need_root

log "Updating package index..."
apt-get update

log "Installing Squid and required tools..."
# squid: proxy server
# apache2-utils: provides htpasswd
# ufw: optional firewall management
DEBIAN_FRONTEND=noninteractive apt-get install -y squid apache2-utils ufw

log "Ensuring Squid configuration directory exists..."
install -d -m 0750 -o root -g proxy /etc/squid || true

log "Creating/updating Squid password file at ${SQUID_PASSWD_FILE} ..."
if [[ ! -f "${SQUID_PASSWD_FILE}" ]]; then
  printf "%s\n" "${AUTH_PASS}" | htpasswd -Bci "${SQUID_PASSWD_FILE}" "${AUTH_USER}"
else
  printf "%s\n" "${AUTH_PASS}" | htpasswd -Bi "${SQUID_PASSWD_FILE}" "${AUTH_USER}"
fi

# On Ubuntu, Squid commonly runs with group "proxy"
chown root:proxy "${SQUID_PASSWD_FILE}"
chmod 0640 "${SQUID_PASSWD_FILE}"

if [[ -f "${SQUID_CONF}" ]]; then
  TS="$(date +%Y%m%d-%H%M%S)"
  BACKUP="${SQUID_CONF}.bak.${TS}"
  log "Backing up existing Squid config to ${BACKUP} ..."
  cp -a "${SQUID_CONF}" "${BACKUP}"
else
  warn "Squid config not found at ${SQUID_CONF}; a new one will be created."
fi

log "Writing Squid configuration..."
cat > "${SQUID_CONF}" <<EOF
# -----------------------------------------------------------------------------
# Squid Configuration for Ubuntu 22.04 x86_64
# Generated on: $(date -Is)
#
# This configuration requires BASIC authentication for all proxy access.
# -----------------------------------------------------------------------------

# Listen on proxy port
http_port ${SQUID_PORT}

# -------------------------------------------------------------------
# Basic Authentication
# -------------------------------------------------------------------
# Ubuntu path for basic_ncsa_auth helper:
auth_param basic program /usr/lib/squid/basic_ncsa_auth ${SQUID_PASSWD_FILE}
auth_param basic realm Squid Proxy Authentication
auth_param basic credentialsttl 2 hours

# ACL for authenticated users
acl authenticated proxy_auth REQUIRED

# -------------------------------------------------------------------
# Safe ports and CONNECT method restrictions
# -------------------------------------------------------------------
acl SSL_ports port 443
acl Safe_ports port 80          # http
acl Safe_ports port 21          # ftp
acl Safe_ports port 443         # https
acl Safe_ports port 70          # gopher
acl Safe_ports port 210         # wais
acl Safe_ports port 1025-65535  # unregistered ports
acl Safe_ports port 280         # http-mgmt
acl Safe_ports port 488         # gss-http
acl Safe_ports port 591         # filemaker
acl Safe_ports port 777         # multiling http
acl CONNECT method CONNECT

# Deny requests to unsafe ports
http_access deny !Safe_ports

# Deny CONNECT to other than SSL ports
http_access deny CONNECT !SSL_ports

# -------------------------------------------------------------------
# Access Rules
# -------------------------------------------------------------------
# Allow only authenticated users
http_access allow authenticated

# Deny everything else
http_access deny all

# -------------------------------------------------------------------
# Logging
# -------------------------------------------------------------------
access_log /var/log/squid/access.log

# -------------------------------------------------------------------
# Cache settings
# -------------------------------------------------------------------
cache_mem 256 MB
maximum_object_size_in_memory 512 KB
cache_dir ufs /var/spool/squid 1000 16 256

# visible_hostname squid-proxy
# -----------------------------------------------------------------------------
EOF

log "Initializing Squid cache directories..."
squid -z || true

log "Validating Squid configuration..."
squid -k parse

log "Enabling and restarting Squid service..."
systemctl enable squid
systemctl restart squid

if cmd_exists ufw; then
  if ufw status | grep -q "Status: active"; then
    log "UFW is active; allowing TCP port ${SQUID_PORT} ..."
    ufw allow "${SQUID_PORT}/tcp"
  else
    warn "UFW is installed but not active. Skipping firewall rule."
  fi
else
  warn "UFW is not installed. Skipping firewall configuration."
fi

log "Squid setup complete."
log "Service status:"
systemctl --no-pager --full status squid || true

cat <<'EOT'

------------------------------------------------------------------------------
Squid proxy has been installed and configured.

Proxy connection details:
  Server: <YOUR_SERVER_IP>
  Port:   3128
  User:   cent
  Pass:   sgsec@1390

Example curl test:
  curl -x http://cent:sgsec@1390@<YOUR_SERVER_IP>:3128 http://example.com

Important files:
  Config: /etc/squid/squid.conf
  Password file: /etc/squid/passwd
  Logs: /var/log/squid/access.log
------------------------------------------------------------------------------

EOT