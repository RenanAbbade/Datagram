#!/usr/bin/env bash
set -euo pipefail
repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
backend_dir="$repo_dir/Datagram WEB/Datagram-backend"
frontend_dir="$repo_dir/Datagram WEB/Datagram-frontend"
(cd "$backend_dir" && ./mvnw -B clean verify)
(cd "$frontend_dir" && npm ci && npm run format:check && npm run build && npm test -- --watch=false)
if [[ "${E2E:-false}" == "true" ]]; then
  (cd "$frontend_dir" && npm run test:e2e)
fi
