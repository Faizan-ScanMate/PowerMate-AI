#!/usr/bin/env bash
set -euo pipefail

gradle testDebugUnitTest --no-daemon --stacktrace
gradle :app:assembleDebug --no-daemon --stacktrace
gradle :app:assembleRelease --no-daemon --stacktrace
gradle :app:bundleRelease --no-daemon --stacktrace
