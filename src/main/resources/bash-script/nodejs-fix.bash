node_major() {
  command -v node >/dev/null 2>&1 || { echo 0; return; }
  node -v | sed 's/^v\([0-9]*\).*/\1/'
}

install_nodejs() {
  local cur; cur=$(node_major)

  if (( cur >= 18 )); then
    info "node v$(node -v | tr -d v) already present (distro package)"
    command -v npm >/dev/null 2>&1 || dnf -y install npm --disablerepo='nodesource*' || true
    return 0
  fi

  # AlmaLinux 10 ships Node 22 in AppStream and has no nodejs module streams.
  # NodeSource conflicts with nodejs-full-i18n, so try the distro repos first.
  info "installing Node.js from distro repositories"
  if dnf -y install nodejs npm --disablerepo='nodesource*' && (( $(node_major) >= 18 )); then
    info "node $(node -v) / npm $(npm -v)"
    return 0
  fi

  warn "distro Node.js unavailable or too old; falling back to NodeSource ${NODE_MAJOR}.x"
  # nodejs-full-i18n pins the exact distro nodejs version and blocks the swap.
  dnf -y remove nodejs-full-i18n nodejs-docs npm nodejs >/dev/null 2>&1 || true
  curl -fsSL "https://rpm.nodesource.com/setup_${NODE_MAJOR}.x" | bash -
  dnf -y install nodejs --allowerasing

  command -v node >/dev/null || die "Node.js installation failed"
  info "node $(node -v) / npm $(npm -v)"
}
