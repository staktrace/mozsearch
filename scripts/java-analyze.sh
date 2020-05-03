#!/usr/bin/env bash

set -x # Show commands
set -eu # Errors/undefined vars are fatal
set -o pipefail # Check all commands in a pipeline

if [ $# -ne 2 ]
then
    echo "Usage: java-analyze.sh config-file.json tree_name"
    exit 1
fi

CONFIG_FILE=$(realpath $1)
TREE_NAME=$2

pushd $MOZSEARCH_PATH/java
./gradlew fatJar
popd
java -cp $MOZSEARCH_PATH/java/build/libs/java-all-0.1.jar org.mozilla.mozsearch.JavaAnalyze "$FILES_ROOT" "$INDEX_ROOT/analysis"
