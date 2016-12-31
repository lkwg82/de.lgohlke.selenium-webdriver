#!/bin/bash

DOCKER_USER_TMP="/tmp/docker_$USER"
DOCKER_M2="$DOCKER_USER_TMP/m2"
DOCKER_WEBDRIVER="$DOCKER_USER_TMP/webdriver"

mkdir -p $DOCKER_M2           # create the directory before
mkdir -p $DOCKER_WEBDRIVER    # to have the correct ownership

args="--name=webdriver-test-$$ -m 1500M --memory-swap=-1 \
    -v /dev/shm:/dev/shm \
    -v $DOCKER_M2:/home/build/.m2/repository \
    -v $DOCKER_WEBDRIVER:/home/build/tmp_webdrivers "

rm -rf target/*
docker build -t test-$$ . | tee docker_build.log
IMAGE_ID=$(tail -n1 docker_build.log| cut -d\  -f3)
CONTAINER_ID=$(docker run --privileged -d $args $IMAGE_ID bash -c 'while true; do sleep 10000; done')

echo -n $IMAGE_ID > docker_IMAGE_ID
echo -n $CONTAINER_ID > docker_CID

function cleanup {
  echo "exit code $?"
  if [ -n "$JENKINS_HOME" ]; then
     echo "don't cleanup leave it for jenkins"
     exit $?
  fi
  ./run_docker_cleanup.sh
}
trap cleanup EXIT INT

UID_OUTSIDE=$(id --user)
GID_OUTSIDE=$(id --group)
USER_INSIDE_DOCKER="build"

docker exec $CONTAINER_ID useradd --uid $UID_OUTSIDE $USER_INSIDE_DOCKER
docker exec $CONTAINER_ID chown -R $USER_INSIDE_DOCKER .
docker exec $CONTAINER_ID su $USER_INSIDE_DOCKER -c './run_tests.sh'



