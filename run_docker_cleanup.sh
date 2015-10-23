#!/bin/bash

CID=$(cat docker_CID)
IMAGE_ID=$(cat docker_IMAGE_ID)

docker cp $CID:/home/build/target target

docker kill  $CID
docker rm -f $CID
docker rmi   $IMAGE_ID