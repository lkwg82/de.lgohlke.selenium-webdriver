#!/bin/bash

set -e

if [ -z "$MAVEN_REPODIR" ]; then
    export MAVEN_REPODIR="-v /tmp/m2_docker:/root/.m2/repository";
fi

args="--name=webdriver-test-$$ -m 1G --memory-swap=-1 -v /tmp/webdriver-tests-$USER:/tmp/webdrivers-root -v /dev/shm/:/dev/shm -v /tmp/webdrivers_docker:/root/tmp_webdrivers $MAVEN_REPODIR "

rm -rf target/*
docker build -t test-$$ . | tee docker_build.log
export IMAGE_ID=$(tail -n1 docker_build.log| cut -d\  -f3)
export CID=$(docker run -d $args $IMAGE_ID bash -c 'while true; do sleep 1; done')

echo -n $IMAGE_ID > docker_IMAGE_ID
echo -n $CID > docker_CID

function cleanup {
  if [ -n "$JENKINS_HOME" ]; then
     echo "don't cleanup leave it for jenkins"
     exit 0
  fi
  ./run_docker_cleanup.sh
}
trap cleanup EXIT INT

docker exec $CID ./run_tests.sh


