#!/bin/bash

set -e

# $ export MAVEN_REPODIR="-v /tmp/m2_docker:/root/.m2/repository"; ./run_tests.sh

if [ -z "$MAVEN_REPODIR" ]; then
    export MAVEN_REPODIR="-v /tmp/m2_docker:/root/.m2/repository";
fi

docker build -t test .
docker run $MAVEN_REPODIR -v /tmp/webdriver-tests-$USER:/tmp/webdrivers-root -ti test ./run_inside_docker.sh