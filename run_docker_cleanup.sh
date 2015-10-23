#!/bin/bash

CID=$(cat docker_CID)
IMAGE_ID=$(cat docker_IMAGE_ID)

rm -rf target
docker cp $CID:/home/build/target .

docker kill  $CID
docker rm -f $CID
docker rmi   $IMAGE_ID