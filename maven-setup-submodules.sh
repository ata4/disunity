#!/usr/bin/env bash
# Build submodules and install to ./maven-repo
# There's probably a better way to do this

MVN_CMD="mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file"
LOCAL_REPO="maven-repo"

# $1: Submodule name
function package()
{
	echo "Packaging $1 ..."

	# Run 'mvn package' and cd back
	cd "lib/$1"
	mvn package
	cd "../.."

	# Assumes only one .jar getting built that matches $1-*.jar
	JAR_PATH="$( ls -rt ./lib/$1/target/$1-*.jar | tail -1 )"
	$MVN_CMD -Dfile="$JAR_PATH" -DlocalRepositoryPath="$LOCAL_REPO"

	return $?
}

package "ioutils"
package "lzmajio"
