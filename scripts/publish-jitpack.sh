#!/bin/bash
set -e

# Only publish for the last 2 Scala 2.12 versions to keep build time reasonable
SCALA_VERSIONS="2.12.20 2.12.21"

JAVA_LIBS="javalibintf javalib"
FULL_SCALA_LIBS="compiler jUnitPlugin scalalib"
JS_LIBS="library irJS linkerInterfaceJS linkerJS testInterface testBridge jUnitRuntime"
JVM_LIBS="ir linkerInterface linker testAdapter"
SCALA_LIBS="$JS_LIBS $JVM_LIBS"

SBT_CMD="sbt"

ARGS=""
for p in $JAVA_LIBS; do
    if [ -z "$ARGS" ]; then
        ARGS="$p/publishM2"
    else
        ARGS="$ARGS; $p/publishM2"
    fi
done
$SBT_CMD "$ARGS"

# Build for each specific Scala version (2.12.20 and 2.12.21)
for scala_ver in $SCALA_VERSIONS; do
    ARGS="++$scala_ver"
    for p in $FULL_SCALA_LIBS; do
        ARGS="$ARGS; ${p}2_12/publishM2"
    done
    $SBT_CMD "$ARGS"
done

ARGS=""
for p in $SCALA_LIBS; do
    if [ -z "$ARGS" ]; then
        ARGS="${p}2_12/publishM2"
    else
        ARGS="$ARGS; ${p}2_12/publishM2"
    fi
done
$SBT_CMD "$ARGS"

$SBT_CMD "sbtPlugin/publishM2"
