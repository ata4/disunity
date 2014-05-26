#!/bin/sh
BASEDIR=$(dirname "$0")
java -jar "$BASEDIR/disunity.jar" "$@"
