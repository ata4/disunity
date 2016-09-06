#!/bin/sh
BASEDIR="$( cd "$( dirname "$( readlink -f ${BASH_SOURCE[0]} )" )" && pwd )"
java -jar "$BASEDIR/disunity.jar" "$@"
