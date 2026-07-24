#!/bin/sh

if [ -z "$MAVEN_HOME" ]; then
  MAVEN_HOME="$(dirname "$0")/.mvn"
fi

if [ -f "$MAVEN_HOME/apache-maven-3.9.9/bin/mvn" ]; then
  "$MAVEN_HOME/apache-maven-3.9.9/bin/mvn" "$@"
elif [ -f "$MAVEN_HOME/bin/mvn" ]; then
  "$MAVEN_HOME/bin/mvn" "$@"
else
  echo "Maven distribution not found. Please run the setup step first." >&2
  exit 1
fi
