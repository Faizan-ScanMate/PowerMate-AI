#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${KEYSTORE_BASE64:-}" ]]; then
  echo "KEYSTORE_BASE64 is missing. Add GitHub secrets for production signing."
  exit 1
fi

echo "$KEYSTORE_BASE64" | base64 --decode > release-keystore.jks
gradle :app:bundleRelease --no-daemon --stacktrace
