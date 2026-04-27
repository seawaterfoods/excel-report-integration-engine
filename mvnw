#!/usr/bin/env bash
# Maven Wrapper
# Minimal mvnw that executes the maven-wrapper jar
if [ -n "$MAVEN_HOME" ]; then
  MVN="$MAVEN_HOME/bin/mvn"
else
  MVN=""
fi
if [ -n "$MVN" ] && [ -x "$MVN" ]; then
  exec "$MVN" "$@"
fi
if [ -z "$JAVA_HOME" ]; then
  JAVA_EXEC=java
else
  JAVA_EXEC="$JAVA_HOME/bin/java"
fi
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
exec "$JAVA_EXEC" -jar "$SCRIPT_DIR/.mvn/wrapper/maven-wrapper.jar" "$@"
