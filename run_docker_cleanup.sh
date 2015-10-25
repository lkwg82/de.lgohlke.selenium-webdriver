#!/bin/bash

CONTAINER_ID=$(cat docker_CID)
IMAGE_ID=$(cat docker_IMAGE_ID)

rm -rf target
docker cp $CONTAINER_ID:/home/build/target .

docker kill  $CONTAINER_ID
docker rm -f $CONTAINER_ID
docker rmi   $IMAGE_ID