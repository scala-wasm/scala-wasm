#! /bin/sh
# Rewrite ScalaJSVersions.current with commit-hash version
#
#   1.22.1-wasm.4-SNAPSHOT  ->  1.22.1-wasm.4-<7-char-sha>-SNAPSHOT
#
# Usage:
#   ./scripts/set-snapshot-version.sh
#
# Optional env: `GIT_SHA`

set -e

# Use gsed on mac
if command -v gsed >/dev/null 2>&1; then
  SED=gsed
else
  SED=sed
fi

VERSIONS_FILE=ir/shared/src/main/scala/org/scalajs/ir/ScalaJSVersions.scala

BASE_VERSION=$($SED -n 's/.*current = "\([^"]*\)".*/\1/p' "$VERSIONS_FILE" | head -1)
case "$BASE_VERSION" in
  *-SNAPSHOT) ;;
  *)
    echo "Can't rewrite non-SNAPSHOT version: $BASE_VERSION" >&2
    exit 1
    ;;
esac

RAW_SHA=${GIT_SHA:-$(git rev-parse HEAD)}
SHORT_SHA=$(echo "$RAW_SHA" | cut -c1-7)
PUBLISH_VERSION=$(echo "$BASE_VERSION" | $SED "s/-SNAPSHOT\$/-${SHORT_SHA}-SNAPSHOT/")

echo "Base version:    $BASE_VERSION" >&2
echo "Publish version: $PUBLISH_VERSION" >&2
echo "Commit:          $RAW_SHA" >&2

$SED -i "s/current = \"$BASE_VERSION\"/current = \"$PUBLISH_VERSION\"/" "$VERSIONS_FILE"
echo "Rewrote $VERSIONS_FILE" >&2

echo "$PUBLISH_VERSION"
