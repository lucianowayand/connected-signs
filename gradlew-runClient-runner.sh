#!/usr/bin/env bash
set -euo pipefail
# Run the project's Gradle wrapper with the runClient task from the repository root.
DIR="$(cd "$(dirname "$0")" && pwd)"
exec "$DIR/gradlew" runClient "$@"
