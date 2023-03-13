#!/bin/sh

# Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
# Licensed under the Apache License, Version 2.0 (the "License")

# Add default JVM options here. You can also use JAVA_OPTS and RIFE2_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

die () {
    echo "$*"
    exit 1
} >&2

# Stop when java is not available

JAVACMD=java
which $JAVACMD >/dev/null 2>&1 || die "ERROR: No '$JAVACMD' command could be found in your PATH."

# Stop when "xargs" is not available.

if ! command -v xargs >/dev/null 2>&1
then
    die "ERROR: xargs is not available"
fi

join_by () {
  SAVE_IFS="$IFS"; IFS="$1"; shift
  echo "$*"
  IFS="$SAVE_IFS"
}

# Construct the classpath

project_jars=$(join_by : $(find "lib/compile" "lib/project" -name "*.jar"))

CLASSPATH="${project_jars}:build/project:src/project/java"

set -- \
        -classpath "$CLASSPATH" \
        src/project/java/com/test/MyappBuild.java \
        "$@"

# Use "xargs" to parse quoted args.
# With -n1 it outputs one arg per line, with the quotes and backslashes removed.
eval "set -- $(
        printf '%s\n' "$DEFAULT_JVM_OPTS $JAVA_OPTS $RIFE2_OPTS" |
        xargs -n1 |
        sed ' s~[^-[:alnum:]+,./:=@_]~\\&~g; ' |
        tr '\n' ' '
    )" '"$@"'

exec "$JAVACMD" "$@"