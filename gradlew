#!/bin/sh
# Minimal launcher for StandBy Clock.
# Preferred: use the Gradle wrapper distribution declared in
# gradle/wrapper/gradle-wrapper.properties (Gradle 8.7).
# In CI (GitHub Actions) the gradle/actions/setup-gradle action installs
# Gradle, and this script delegates to it. Do NOT rely on this script
# alone to bootstrap Gradle on a bare machine — run `gradle build`
# with Gradle 8.7 installed, or generate a full wrapper with
# `gradle wrapper --gradle-version 8.7` before first use.
set -e
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
GRADLE_HOME="${GRADLE_HOME:-$HOME/.gradle/wrapper/dists/gradle-8.7-bin}"
GRADLE_BIN=$(find "$GRADLE_HOME" -maxdepth 3 -name gradle -type f 2>/dev/null | head -n 1 || true)
if [ -n "$GRADLE_BIN" ]; then
  exec "$GRADLE_BIN" "$@"
fi
echo "Gradle not found. Install Gradle 8.7 or run via gradle/actions/setup-gradle in CI." >&2
exit 1
