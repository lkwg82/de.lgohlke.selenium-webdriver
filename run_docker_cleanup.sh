#!/bin/bash

CONTAINER_ID=$(cat docker_CID)
IMAGE_ID=$(cat docker_IMAGE_ID)

rm -rf target
docker cp $CONTAINER_ID:/home/build/target .
docker cp $CONTAINER_ID:/home/build/installed_software.log target

# check for issues (low memory etc.)
if [ $(docker exec $CONTAINER_ID dmesg -T | grep docker-$CONTAINER_ID | wc -l) -gt 0 ]; then
    echo "";
    echo "[ERROR] there were issues with $CONTAINER_ID:";
    echo "";
    docker exec $CONTAINER_ID dmesg -T | grep docker-$CONTAINER_ID
    echo "--"
    echo "host config looks like (reduced)"
    docker inspect --format="{{json .HostConfig}}" $CONTAINER_ID | python -m json.tool \
        | grep -v ": \"\"" \
        | grep -v ": null" \
        | grep -vE ": 0|-1" \
        | grep -v ": \[\]" \
        | grep -v ": {}"

    exit 137
fi

# cleanup instances
docker kill  $CONTAINER_ID
docker rm -f $CONTAINER_ID
docker rmi   $IMAGE_ID